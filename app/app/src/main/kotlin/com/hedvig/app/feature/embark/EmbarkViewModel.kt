package com.hedvig.app.feature.embark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.common.android.ProgressPercentage
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.android.core.common.android.asMap
import com.hedvig.app.feature.embark.extensions.api
import com.hedvig.app.feature.embark.extensions.getComputedValues
import com.hedvig.app.feature.embark.util.SelectedContractType
import com.hedvig.app.feature.embark.util.evaluateExpression
import com.hedvig.app.feature.embark.util.getFileVariables
import com.hedvig.app.feature.embark.util.getOfferKeyOrNull
import com.hedvig.app.feature.embark.util.getSelectedContractTypes
import com.hedvig.app.feature.embark.util.getVariables
import com.hedvig.app.feature.embark.util.toExpressionFragment
import com.hedvig.app.util.safeLet
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import giraffe.EmbarkStoryQuery
import giraffe.fragment.ApiFragment
import giraffe.fragment.MessageFragment
import giraffe.type.EmbarkExternalRedirectLocation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack
import kotlin.math.max

const val QUOTE_CART_EMBARK_STORE_ID_KEY = "quoteCartId"

abstract class EmbarkViewModel(
  private val valueStore: ValueStore,
  private val graphQLQueryUseCase: GraphQLQueryUseCase,
  private val hAnalytics: HAnalytics,
  val storyName: String,
  authTokenService: AuthTokenService,
) : ViewModel() {
  private val _passageState = MutableLiveData<PassageState>()
  private val passageState: LiveData<PassageState> = _passageState

  private val loginStatus: StateFlow<AuthStatus?> = authTokenService.authStatus

  val viewState: LiveData<ViewState> = combine(passageState.asFlow(), loginStatus) { passageState, loginStatus ->
    ViewState(
      passageState,
      loginStatus is AuthStatus.LoggedIn,
    )
  }.asLiveData()
  protected val _events = Channel<Event>(Channel.UNLIMITED)

  val events = _events.receiveAsFlow()
  private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)

  val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

  data class ViewState(
    val passageState: PassageState,
    val isLoggedIn: Boolean,
  )

  data class PassageState(
    val passage: EmbarkStoryQuery.Passage?,
    val navigationDirection: NavigationDirection,
    val progressPercentage: ProgressPercentage,
    val hasTooltips: Boolean,
  )

  sealed class Event {
    data class Offer(
      val quoteCartId: QuoteCartId,
      val selectedContractTypes: List<SelectedContractType>,
    ) : Event()

    data class Error(val message: String? = null) : Event()
    object Close : Event()
    object Chat : Event()
  }

  abstract fun fetchStory(name: String)

  protected lateinit var storyData: EmbarkStoryQuery.Data

  private val backStack = Stack<String>()
  private var totalSteps: Int = 0

  init {
    hAnalytics.screenView(AppScreen.EMBARK)
  }

  protected fun setInitialState() {
    storyData.embarkStory?.let { story ->
      valueStore.computedValues = story.getComputedValues()
      val firstPassage = story.passages.first { it.id == story.startPassage }

      totalSteps = getPassagesLeft(firstPassage)

      viewModelScope.launch {
        navigateToPassage(firstPassage.name)
      }
    }
  }

  fun putInStore(key: String, value: String?) {
    valueStore.put(key, value)
  }

  fun putInStore(key: String, value: List<String>) {
    valueStore.put(key, value)
  }

  fun getStoreAsMap() = valueStore.toMap()

  fun getPrefillFromStore(key: String) = valueStore.prefill.get(key)

  fun submitAction(nextPassageName: String, submitIndex: Int = 0) {
    val apiFromAction = viewState.value?.passageState?.passage?.action?.api(submitIndex)
    if (apiFromAction != null) {
      callApi(apiFromAction)
    } else {
      viewModelScope.launch {
        navigateToPassage(nextPassageName)
      }
    }
  }

  private suspend fun navigateToPassage(passageName: String) {
    val nextPassage = storyData.embarkStory?.passages?.find { it.name == passageName }
    val redirectPassage = getRedirectPassageAndPutInStore(nextPassage?.redirects)
    val location = nextPassage?.externalRedirect?.data?.location
    val api = nextPassage?.api?.fragments?.apiFragment

    val key = nextPassage?.getOfferKeyOrNull(valueStore)

    when {
      storyData.embarkStory == null || nextPassage == null -> _events.trySend(Event.Error())
      redirectPassage != null -> navigateToPassage(redirectPassage)
      key != null && key.isNotEmpty() -> {
        // For offers, there is a problem with the Offer screen not committing before this stage is reached,
        //  meaning that the old values were returned from getList/get.
        valueStore.withCommittedVersion {
          val quoteCartId = this.get(QUOTE_CART_EMBARK_STORE_ID_KEY)?.let { QuoteCartId(it) }
          if (quoteCartId == null) {
            _events.trySend(Event.Error())
          } else {
            val selectedContractTypes = nextPassage.getSelectedContractTypes(this)
            _events.trySend(Event.Offer(quoteCartId, selectedContractTypes))
          }
        }
      }
      location != null -> handleRedirectLocation(location)
      api != null -> callApi(api)
      else -> setupPassageAndEmitState(nextPassage)
    }
  }

  private fun setupPassageAndEmitState(nextPassage: EmbarkStoryQuery.Passage) {
    _passageState.value?.passage?.name?.let {
      valueStore.commitVersion()
      backStack.push(it)
    }
    val passageState = PassageState(
      passage = preProcessPassage(nextPassage),
      navigationDirection = NavigationDirection.FORWARDS,
      progressPercentage = currentProgress(nextPassage),
      hasTooltips = nextPassage.tooltips.isNotEmpty(),
    )
    _passageState.postValue(passageState)
    _loadingState.update { false }
    nextPassage.tracks.forEach { track ->
      hAnalytics.embarkTrack(storyName, track.eventName, trackingData(track))
    }
  }

  private fun callApi(apiFragment: ApiFragment) {
    _loadingState.update { true }

    val graphQLQuery = apiFragment.asEmbarkApiGraphQLQuery
    val graphQLMutation = apiFragment.asEmbarkApiGraphQLMutation
    when {
      graphQLQuery != null -> handleGraphQLQuery(graphQLQuery)
      graphQLMutation != null -> handleGraphQLMutation(graphQLMutation)
      else -> {
        _loadingState.update { false }
        _events.trySend(Event.Error())
      }
    }
  }

  private fun handleGraphQLQuery(graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery) {
    viewModelScope.launch {
      val (variables, fileVariables) = valueStore.withCommittedVersion {
        val variables = graphQLQuery.getVariables(valueStore)
        val fileVariables = graphQLQuery.getFileVariables(valueStore)
        variables to fileVariables
      }
      val result = graphQLQueryUseCase.executeQuery(graphQLQuery, variables, fileVariables)
      handleQueryResult(result)
    }
  }

  private fun handleGraphQLMutation(graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation) {
    viewModelScope.launch {
      val (variables, fileVariables) = valueStore.withCommittedVersion {
        val variables = graphQLMutation.getVariables(this)
        val fileVariables = graphQLMutation.getFileVariables(this)
        variables to fileVariables
      }
      val result = graphQLQueryUseCase.executeMutation(graphQLMutation, variables, fileVariables)
      handleQueryResult(result)
    }
  }

  private fun handleQueryResult(result: GraphQLQueryResult) {
    _loadingState.update { false }

    when (result) {
      // TODO Handle errors
      is GraphQLQueryResult.Error -> viewModelScope.launch {
        navigateToPassage(result.passageName)
      }
      is GraphQLQueryResult.ValuesFromResponse -> {
        result.arrayValues.forEach {
          valueStore.put(it.first, it.second)
        }
        result.objectValues.forEach {
          valueStore.put(it.first, it.second)
        }

        if (result.passageName != null) {
          viewModelScope.launch {
            navigateToPassage(result.passageName)
          }
        }
      }
    }
  }

  private fun handleRedirectLocation(location: EmbarkExternalRedirectLocation) {
    hAnalytics.embarkExternalRedirect(location.rawValue)
    viewModelScope.launch {
      val event = when (location) {
        EmbarkExternalRedirectLocation.Offer -> createOfferEvent()
        EmbarkExternalRedirectLocation.Chat -> Event.Chat
        EmbarkExternalRedirectLocation.Close -> Event.Close
        else -> null
      }
      event?.let(_events::trySend)
    }
  }

  private fun createOfferEvent(): Event {
    val quoteCartId = valueStore.get(QUOTE_CART_EMBARK_STORE_ID_KEY)?.let(::QuoteCartId)
    return if (quoteCartId != null) {
      Event.Offer(
        quoteCartId = quoteCartId,
        selectedContractTypes = emptyList(),
      )
    } else {
      Event.Error()
    }
  }

  private fun getRedirectPassageAndPutInStore(redirects: List<EmbarkStoryQuery.Redirect>?): String? {
    redirects?.forEach { redirect ->
      if (evaluateExpression(redirect.toExpressionFragment(), valueStore) is ExpressionResult.True) {
        redirect.passedKeyValue?.let { (key, value) -> putInStore(key, value) }
        redirect.to?.let { to ->
          return to
        }
      }
    }
    return null
  }

  private fun trackingData(track: EmbarkStoryQuery.Track): Map<String, String> = when {
    track.includeAllKeys -> valueStore.toMap()
    track.eventKeys.filterNotNull().isNotEmpty() ->
      track
        .eventKeys
        .filterNotNull()
        .associateWith { valueStore.get(it) }
    else -> emptyMap()
  }.let { data ->
    track.customData?.let { data + it.asMap() } ?: data
  }.map { it.key to it.value.toString() }.toMap()

  private fun currentProgress(passage: EmbarkStoryQuery.Passage?): ProgressPercentage {
    if (passage == null) {
      return ProgressPercentage(0f)
    }
    val passagesLeft = getPassagesLeft(passage)
    val progress = ((totalSteps.toFloat() - passagesLeft.toFloat()) / totalSteps.toFloat())
    return ProgressPercentage.safeValue(progress)
  }

  fun navigateBack(): Boolean {
    if (backStack.isEmpty()) {
      return false
    }
    val passageName = backStack.pop()

    storyData.embarkStory?.let { story ->
      _passageState.value?.passage?.name?.let { currentPassageName ->
        hAnalytics.embarkPassageGoBack(storyName, currentPassageName)
      }
      val nextPassage = story.passages.find { it.name == passageName }
      val passageState = PassageState(
        passage = preProcessPassage(nextPassage),
        navigationDirection = NavigationDirection.BACKWARDS,
        progressPercentage = currentProgress(nextPassage),
        hasTooltips = nextPassage?.tooltips?.isNotEmpty() == true,
      )
      _loadingState.update { false }
      _passageState.postValue(passageState)

      valueStore.rollbackVersion()
      return true
    }
    return false
  }

  fun preProcessResponse(passageName: String): Response? {
    return valueStore.withCommittedVersion {
      preProcessResponse(passageName, this)
    }
  }

  private fun preProcessResponse(
    passageName: String,
    valueStore: ValueStore = this.valueStore,
  ): Response? {
    val response = storyData
      .embarkStory
      ?.passages
      ?.find { it.name == passageName }
      ?.response
      ?: return null

    response.fragments.messageFragment?.let { message ->
      preProcessMessage(message, valueStore)?.let { return Response.SingleResponse(it.text) }
    }

    response.fragments.responseExpressionFragment?.let { exp ->
      preProcessMessage(
        MessageFragment(
          text = exp.text,
          expressions = exp.expressions.map {
            MessageFragment.Expression(
              __typename = it.__typename,
              fragments = MessageFragment.Expression.Fragments(it.fragments.expressionFragment),
            )
          },
        ),
        valueStore,
      )?.let { return Response.SingleResponse(it.text) }
    }

    response.fragments.groupedResponseFragment?.let { groupedResponse ->
      val titleExpression = groupedResponse.title.fragments.responseExpressionFragment
      val title = preProcessMessage(
        MessageFragment(
          text = titleExpression.text,
          expressions = titleExpression.expressions.map {
            MessageFragment.Expression(
              __typename = it.__typename,
              fragments = MessageFragment.Expression.Fragments(it.fragments.expressionFragment),
            )
          },
        ),
        valueStore,
      )?.text

      val items = groupedResponse.items.mapNotNull { item ->
        preProcessMessage(item.fragments.messageFragment, valueStore)?.text
      }.toMutableList()

      groupedResponse.each?.let { each ->
        val multiActionItems = valueStore.getMultiActionItems(each.key)
        items += multiActionItems.mapNotNull { mai ->
          val maiView = object : ValueStoreView {
            override fun get(key: String) = mai[key]
            override fun getList(key: String): List<String>? = null
          }
          preProcessMessage(each.content.fragments.messageFragment, maiView)?.text
        }
      }

      return Response.GroupedResponse(
        title = title,
        groups = items,
      )
    }
    return null
  }

  private fun preProcessPassage(passage: EmbarkStoryQuery.Passage?): EmbarkStoryQuery.Passage? {
    if (passage == null) {
      return null
    }

    return passage.copy(
      messages = passage.messages.mapNotNull { message ->
        val messageFragment =
          preProcessMessage(message.fragments.messageFragment, valueStore) ?: return@mapNotNull null
        message.copy(
          fragments = EmbarkStoryQuery.Message.Fragments(messageFragment),
        )
      },
    )
  }

  private fun getPassagesLeft(passage: EmbarkStoryQuery.Passage) = passage.allLinks
    .map { findMaxDepth(it.fragments.embarkLinkFragment.name) }
    .fold(0) { acc, i -> max(acc, i) }

  private fun findMaxDepth(passageName: String, previousDepth: Int = 0): Int {
    val passage = storyData.embarkStory?.passages?.find { it.name == passageName }
    val links = passage?.allLinks?.map { it.fragments.embarkLinkFragment }

    if (links?.size == 0 || links == null) {
      return previousDepth
    }

    return links
      .filter { !it.hidden }
      .map { findMaxDepth(it.name, previousDepth + 1) }
      .fold(0) { acc, i -> max(acc, i) }
  }

  private fun preProcessMessage(
    message: MessageFragment,
    valueStoreView: ValueStoreView,
  ): MessageFragment? {
    if (message.expressions.isEmpty()) {
      return message.copy(
        text = interpolateMessage(message.text, valueStoreView),
      )
    }

    val expressionText = message
      .expressions
      .map { evaluateExpression(it.fragments.expressionFragment, valueStore) }
      .filterIsInstance<ExpressionResult.True>()
      .firstOrNull()
      ?.resultValue
      ?: return null

    return message.copy(
      text = interpolateMessage(expressionText),
    )
  }

  private fun interpolateMessage(message: String, store: ValueStoreView = valueStore) =
    REPLACEMENT_FINDER
      .findAll(message)
      .fold(message) { acc, curr ->
        val key = curr.value.removeSurrounding("{", "}")
        val fromStore = store.get(key) ?: return@fold acc
        acc.replace(curr.value, fromStore)
      }

  companion object {
    private val REPLACEMENT_FINDER = Regex("\\{[\\w.]+\\}")

    private val EmbarkStoryQuery.Redirect.to: String?
      get() {
        asEmbarkRedirectUnaryExpression?.let { return it.to }
        asEmbarkRedirectBinaryExpression?.let { return it.to }
        asEmbarkRedirectMultipleExpressions?.let { return it.to }

        return null
      }

    private val EmbarkStoryQuery.Redirect.passedKeyValue: Pair<String, String>?
      get() {
        asEmbarkRedirectUnaryExpression?.let { asUnary ->
          return safeLet(
            asUnary.passedExpressionKey,
            asUnary.passedExpressionValue,
          ) { key, value -> Pair(key, value) }
        }
        asEmbarkRedirectBinaryExpression?.let { asBinary ->
          return safeLet(
            asBinary.passedExpressionKey,
            asBinary.passedExpressionValue,
          ) { key, value -> Pair(key, value) }
        }
        asEmbarkRedirectMultipleExpressions?.let { asMultiple ->
          return safeLet(
            asMultiple.passedExpressionKey,
            asMultiple.passedExpressionValue,
          ) { key, value -> Pair(key, value) }
        }
        return null
      }
  }
}

class EmbarkViewModelImpl(
  private val embarkRepository: EmbarkRepository,
  authTokenService: AuthTokenService,
  graphQLQueryUseCase: GraphQLQueryUseCase,
  valueStore: ValueStore,
  hAnalytics: HAnalytics,
  storyName: String,
) : EmbarkViewModel(
  valueStore,
  graphQLQueryUseCase,
  hAnalytics,
  storyName,
  authTokenService,
) {

  init {
    fetchStory(storyName)
  }

  override fun fetchStory(name: String) {
    viewModelScope.launch {
      embarkRepository.embarkStory(name).fold(
        ifLeft = { _events.trySend(Event.Error(it.message)) },
        ifRight = {
          storyData = it
          setInitialState()
        },
      )
    }
  }
}

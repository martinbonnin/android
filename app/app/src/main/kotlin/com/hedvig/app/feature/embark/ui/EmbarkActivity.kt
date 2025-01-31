package com.hedvig.app.feature.embark.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.hide
import com.hedvig.android.core.common.android.remove
import com.hedvig.android.core.common.android.whenApiVersion
import com.hedvig.android.market.MarketManager
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityEmbarkBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.NavigationDirection
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
import com.hedvig.app.feature.embark.passages.addressautocomplete.EmbarkAddressAutoCompleteFragment
import com.hedvig.app.feature.embark.passages.addressautocomplete.EmbarkAddressAutoCompleteParams
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderFragment
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderParameters
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerFragment
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerParams
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerFragment
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerParameter
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionComponent
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionFragment
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.feature.embark.passages.noaction.NoActionFragment
import com.hedvig.app.feature.embark.passages.noaction.NoActionParameter
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionFragment
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionParams
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerFragment
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerParameter
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionFragment
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionParameter
import com.hedvig.app.feature.embark.passages.textaction.TextActionFragment
import com.hedvig.app.feature.embark.passages.textaction.TextActionParameter
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.startChat
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.navigation.openAuth
import giraffe.EmbarkStoryQuery
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

class EmbarkActivity : AppCompatActivity(R.layout.activity_embark) {

  private val storyTitle: String by lazy {
    intent.getStringExtra(STORY_TITLE)
      ?: error("Programmer error: STORY_TITLE not provided to ${this.javaClass.name}")
  }

  private val storyName: String by lazy {
    intent.getStringExtra(STORY_NAME)
      ?: error("Programmer error: STORY_NAME not provided to ${this.javaClass.name}")
  }

  private val viewModel: EmbarkViewModel by viewModel { parametersOf(storyName) }
  private val binding by viewBinding(ActivityEmbarkBinding::bind)
  private val marketManager: MarketManager by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    onBackPressedDispatcher.addCallback(this) {
      val couldNavigateBack = viewModel.navigateBack()
      if (!couldNavigateBack) {
        remove()
        onBackPressedDispatcher.onBackPressed()
      }
    }

    binding.apply {
      whenApiVersion(Build.VERSION_CODES.R) {
        window.compatSetDecorFitsSystemWindows(false)
        progressToolbar.applyStatusBarInsets()
      }
      progressToolbar.toolbar.title = storyTitle

      viewModel.viewState.observe(this@EmbarkActivity) { viewState ->
        loadingSpinnerLayout.loadingSpinner.remove() // Removing inner spinner on first available viewState
        setupToolbarMenu(
          progressToolbar,
          viewState.passageState.hasTooltips,
          viewState.isLoggedIn,
        )
        progressToolbar.setProgress(viewState.passageState.progressPercentage)

        val passage = viewState.passageState.passage
        supportActionBar?.title = passage?.name

        lifecycleScope.launch {
          transitionToNextPassage(viewState.passageState.navigationDirection, passage)
        }
      }

      viewModel
        .loadingState
        .flowWithLifecycle(lifecycle)
        .onEach { isLoading ->
          fullScreenLoadingSpinnerLayout.isVisible = isLoading
        }
        .launchIn(lifecycleScope)

      viewModel
        .events
        .flowWithLifecycle(lifecycle)
        .onEach { event ->
          when (event) {
            EmbarkViewModel.Event.Chat -> startChat()
            is EmbarkViewModel.Event.Offer -> {
              startActivity(
                OfferActivity.newInstance(
                  context = this@EmbarkActivity,
                  quoteCartId = event.quoteCartId,
                  selectedContractTypes = event.selectedContractTypes,
                ),
              )
            }

            is EmbarkViewModel.Event.Error -> {
              fullScreenLoadingSpinnerLayout.hide()
              AlertDialog.Builder(this@EmbarkActivity)
                .setTitle(com.adyen.checkout.dropin.R.string.error_dialog_title)
                .setMessage(event.message ?: getString(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE))
                .setPositiveButton(com.adyen.checkout.dropin.R.string.error_dialog_button) { _, _ ->
                  this@EmbarkActivity.finish()
                }
                .create()
                .show()
            }

            EmbarkViewModel.Event.Close -> {
              finish()
            }
          }
        }
        .launchIn(lifecycleScope)

      progressToolbar.toolbar.apply {
        setOnMenuItemClickListener(::handleMenuItem)
        setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
      }
    }
  }

  private fun handleMenuItem(menuItem: MenuItem) = when (menuItem.itemId) {
    R.id.login -> {
      marketManager.market?.openAuth(this, supportFragmentManager)
      true
    }

    R.id.exit -> {
      showExitDialog()
      true
    }

    R.id.tooltip -> {
      viewModel.viewState.value?.passageState?.passage?.tooltips?.let {
        TooltipBottomSheet.newInstance(it).show(
          supportFragmentManager,
          TooltipBottomSheet.TAG,
        )
      }
      true
    }

    else -> false
  }

  private fun showExitDialog() {
    MaterialAlertDialogBuilder(this)
      .setMessage(hedvig.resources.R.string.EMBARK_EXIT_DIALOG_MESSAGE)
      .setPositiveButton(hedvig.resources.R.string.EMBARK_EXIT_DIALOG_POSITIVE_BUTTON) { _, _ -> finish() }
      .setNegativeButton(hedvig.resources.R.string.EMBARK_EXIT_DIALOG_NEGATIVE_BUTTON) { dialog, _ -> dialog.dismiss() }
      .show()
  }

  private fun setupToolbarMenu(
    progressToolbar: MaterialProgressToolbar,
    hasToolTips: Boolean,
    isLoggedIn: Boolean,
  ) {
    invalidateOptionsMenu()
    with(progressToolbar.toolbar) {
      menu.clear()
      inflateMenu(R.menu.embark_menu)
      menu.findItem(R.id.tooltip).isVisible = hasToolTips
      menu.findItem(R.id.login).isVisible = !isLoggedIn
    }
  }

  private fun transitionToNextPassage(
    navigationDirection: NavigationDirection,
    passage: EmbarkStoryQuery.Passage?,
  ) {
    supportFragmentManager
      .findFragmentByTag("passageFragment")
      ?.exitTransition = MaterialSharedAxis(SHARED_AXIS, navigationDirection == NavigationDirection.FORWARDS)

    val newFragment = passageFragment(passage)

    val transition: Transition = when (navigationDirection) {
      NavigationDirection.FORWARDS,
      NavigationDirection.BACKWARDS,
      -> {
        MaterialSharedAxis(SHARED_AXIS, navigationDirection == NavigationDirection.FORWARDS)
      }

      NavigationDirection.INITIAL -> MaterialFadeThrough()
    }

    newFragment.enterTransition = transition

    supportFragmentManager
      .beginTransaction()
      .replace(R.id.passageContainer, newFragment, "passageFragment")
      .commit()
  }

  private fun passageFragment(passage: EmbarkStoryQuery.Passage?): Fragment {
    passage?.action?.asEmbarkSelectAction?.let { options ->
      val parameter = SelectActionParameter.from(
        passage.messages.map { it.fragments.messageFragment.text },
        options.selectData,
        passage.name,
      )
      return SelectActionFragment.newInstance(parameter)
    }

    passage?.action?.asEmbarkTextAction?.let { textAction ->
      val parameter = TextActionParameter.from(
        passage.messages.map { it.fragments.messageFragment.text },
        textAction.textData,
        passage.name,
      )
      return TextActionFragment.newInstance(parameter)
    }

    passage?.action?.asEmbarkTextActionSet?.let { textActionSet ->
      textActionSet.textSetData?.let { data ->
        val parameter = TextActionParameter.from(
          passage.messages.map { it.fragments.messageFragment.text },
          data,
          passage.name,
        )
        return TextActionFragment.newInstance(parameter)
      }
    }

    passage?.action?.asEmbarkPreviousInsuranceProviderAction?.let { previousInsuranceAction ->
      val parameter = PreviousInsurerParameter.from(
        passage.messages.map { it.fragments.messageFragment.text },
        previousInsuranceAction,
      )
      return PreviousInsurerFragment.newInstance(parameter)
    }

    passage?.action?.asEmbarkExternalInsuranceProviderAction?.let { externalInsuranceAction ->
      val parameter = ExternalInsurerParameter.from(
        passage.messages.map { it.fragments.messageFragment.text },
        externalInsuranceAction,
      )
      return ExternalInsurerFragment.newInstance(parameter)
    }

    passage?.action?.asEmbarkNumberAction?.numberActionData?.let { numberAction ->
      return NumberActionFragment.newInstance(
        NumberActionParams(
          passage.messages.map { it.fragments.messageFragment.text },
          passage.name,
          listOf(
            NumberActionParams.NumberAction(
              key = numberAction.fragments.embarkNumberActionFragment.key,
              title = numberAction.fragments.embarkNumberActionFragment.label,
              placeholder = numberAction.fragments.embarkNumberActionFragment.placeholder,
              unit = numberAction.fragments.embarkNumberActionFragment.unit,
              maxValue = numberAction.fragments.embarkNumberActionFragment.maxValue,
              minValue = numberAction.fragments.embarkNumberActionFragment.minValue,
            ),
          ),
          link = numberAction.fragments.embarkNumberActionFragment.link.fragments.embarkLinkFragment.name,
          submitLabel = numberAction.fragments.embarkNumberActionFragment
            .link.fragments.embarkLinkFragment.label,
        ),
      )
    }

    passage?.action?.asEmbarkNumberActionSet?.numberActionSetData?.let { numberActionSet ->
      return NumberActionFragment.newInstance(
        NumberActionParams(
          passage.messages.map { it.fragments.messageFragment.text },
          passage.name,
          numberActions = numberActionSet.numberActions.map { numberAction ->
            NumberActionParams.NumberAction(
              key = numberAction.data!!.key,
              title = numberAction.data!!.title,
              placeholder = numberAction.data!!.placeholder,
              unit = numberAction.data!!.unit,
              maxValue = numberAction.data!!.maxValue,
              minValue = numberAction.data!!.minValue,
            )
          },
          link = numberActionSet.link.fragments.embarkLinkFragment.name,
          submitLabel = numberActionSet.link.fragments.embarkLinkFragment.label,
        ),
      )
    }

    passage?.action?.asEmbarkDatePickerAction?.let { datePickerAction ->
      val params = DatePickerParams(
        passage.messages.map { it.fragments.messageFragment.text },
        passage.name,
        datePickerAction.storeKey,
        datePickerAction.label,
        datePickerAction.label,
        datePickerAction.next.fragments.embarkLinkFragment.name,
      )
      return DatePickerFragment.newInstance(params)
    }

    passage?.action?.asEmbarkMultiAction?.let { multiAction ->
      val params = MultiActionParams(
        key = multiAction.multiActionData.key ?: "",
        link = multiAction.multiActionData.link.fragments.embarkLinkFragment.name,
        addLabel = multiAction.multiActionData.addLabel
          ?: getString(com.adyen.checkout.dropin.R.string.continue_button),
        maxAmount = multiAction.multiActionData.maxAmount.toInt(),
        messages = passage.messages.map { it.fragments.messageFragment.text },
        passageName = passage.name,
        components = multiAction.multiActionData.components.map {
          val dropDownActionData = it.asEmbarkDropdownAction?.dropDownActionData
          val switchActionData = it.asEmbarkSwitchAction?.switchActionData
          val numberActionData = it.asEmbarkMultiActionNumberAction?.numberActionData

          when {
            dropDownActionData != null -> MultiActionComponent.Dropdown(
              dropDownActionData.key,
              dropDownActionData.label,
              dropDownActionData.options.map {
                MultiActionComponent.Dropdown.Option(it.text, it.value)
              },
            )

            switchActionData != null -> MultiActionComponent.Switch(
              switchActionData.key,
              switchActionData.label,
              switchActionData.defaultValue,
            )

            numberActionData != null -> MultiActionComponent.Number(
              numberActionData.key,
              numberActionData.placeholder,
              numberActionData.unit,
              numberActionData.label,
            )

            else -> error(
              "Could not match $it to a component",
            )
          }
        },
        submitLabel = multiAction.multiActionData.link.fragments.embarkLinkFragment.label,
      )
      return MultiActionFragment.newInstance(params)
    }

    passage?.action?.asEmbarkAudioRecorderAction?.let { audioRecorderAction ->
      val params = AudioRecorderParameters(
        messages = passage.messages.map { it.fragments.messageFragment.text },
        key = audioRecorderAction.audioRecorderActionData.storeKey,
        label = audioRecorderAction.audioRecorderActionData.label,
        link = audioRecorderAction.audioRecorderActionData.next.fragments.embarkLinkFragment.name,
      )
      return AudioRecorderFragment.newInstance(params)
    }

    passage?.action?.asEmbarkAddressAutocompleteAction?.let { addressAutocompleteAction ->
      val params = EmbarkAddressAutoCompleteParams(
        messages = passage.messages.map { it.fragments.messageFragment.text },
        key = addressAutocompleteAction.addressAutocompleteActionData.key,
        placeholder = addressAutocompleteAction.addressAutocompleteActionData.placeholder,
        link = addressAutocompleteAction.addressAutocompleteActionData.link
          .fragments.embarkLinkFragment.name,
      )
      return EmbarkAddressAutoCompleteFragment.newInstance(params)
    }

    if (passage?.messages?.isNotEmpty() == true) {
      val params = NoActionParameter(passage.messages.map { it.fragments.messageFragment.text })
      return NoActionFragment.newInstance(params)
    }

    return UpgradeAppFragment.newInstance()
  }

  companion object {
    private const val SHARED_AXIS = MaterialSharedAxis.X
    internal const val STORY_NAME = "STORY_NAME"
    internal const val STORY_TITLE = "STORY_TITLE"
    internal val PASSAGE_ANIMATION_DELAY_DURATION = 150.milliseconds
    internal val KEYBOARD_HIDE_DELAY_DURATION = 450.milliseconds

    fun newInstance(context: Context, storyName: String, storyTitle: String): Intent =
      Intent(context, EmbarkActivity::class.java).apply {
        putExtra(STORY_NAME, storyName)
        putExtra(STORY_TITLE, storyTitle)
      }
  }
}

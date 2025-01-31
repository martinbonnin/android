package com.hedvig.android.feature.profile.aboutapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import giraffe.MemberIdQuery
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

internal class AboutAppViewModel(
  hAnalytics: HAnalytics,
  apolloClient: ApolloClient,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.APP_INFORMATION)
  }

  val uiState: StateFlow<AboutAppUiState> = flow {
    val memberId = apolloClient
      .query(MemberIdQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .getOrNull()
      ?.member
      ?.id
    emit(AboutAppUiState(memberId))
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    AboutAppUiState(null),
  )
}

internal data class AboutAppUiState(
  val memberId: String?,
)

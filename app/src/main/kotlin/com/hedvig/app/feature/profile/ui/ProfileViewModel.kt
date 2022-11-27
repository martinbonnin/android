package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.ui.tab.ProfileQueryDataToProfileUiStateMapper
import com.hedvig.app.feature.profile.ui.tab.ProfileUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.e
import kotlin.time.Duration.Companion.seconds

class ProfileViewModel(
  private val profileRepository: ProfileRepository,
  private val logoutUseCase: LogoutUseCase,
  private val profileQueryDataToProfileUiStateMapper: ProfileQueryDataToProfileUiStateMapper,
) : ViewModel() {
  sealed interface ViewState {
    data class Success(val profileUiState: ProfileUiState) : ViewState
    object Error : ViewState
    object Loading : ViewState
  }

  val dirty: MutableLiveData<Boolean> = MutableLiveData(false)

  private val observeProfileRetryChannel = RetryChannel()
  val data: StateFlow<ViewState> = observeProfileRetryChannel
    .flatMapLatest {
      profileRepository
        .profile()
        .mapLatest { profileQueryDataResult ->
          profileQueryDataResult.map { profileQueryDataToProfileUiStateMapper.map(it) }
        }
        .mapLatest { profileUiStateResult ->
          when (profileUiStateResult) {
            is Either.Left -> {
              ViewState.Error
            }
            is Either.Right -> {
              ViewState.Success(profileUiStateResult.value)
            }
          }
        }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = ViewState.Loading,
    )

  fun saveInputs(emailInput: String, phoneNumberInput: String) {
    var (email, phoneNumber) =
      (data.value as? ViewState.Success)?.profileUiState?.member?.let { Pair(it.email, it.phoneNumber) }
        ?: Pair(null, null)
    viewModelScope.launch {
      if (email != emailInput) {
        val response =
          runCatching { profileRepository.updateEmail(emailInput) }
        if (response.isFailure) {
          response.exceptionOrNull()?.let { e(it) { "$it error updating email" } }
          return@launch
        }
        response.getOrNull()?.let {
          email = it.data?.updateEmail?.email
        }
      }

      if (phoneNumber != phoneNumberInput) {
        val response = runCatching {
          profileRepository.updatePhoneNumber(phoneNumberInput)
        }
        if (response.isFailure) {
          response.exceptionOrNull()?.let { e(it) { "$it error updating phone number" } }
          return@launch
        }
        response.getOrNull()?.let {
          phoneNumber = it.data?.updatePhoneNumber?.phoneNumber
        }
      }

      profileRepository.writeEmailAndPhoneNumberInCache(email, phoneNumber)
    }
  }

  fun reload() {
    observeProfileRetryChannel.retry()
  }

  fun emailChanged(newEmail: String) {
    if (currentEmailOrEmpty() != newEmail && dirty.value != true) {
      dirty.value = true
    }
  }

  private fun currentEmailOrEmpty() = (data.value as? ViewState.Success)?.profileUiState?.member?.email ?: ""

  private fun currentPhoneNumberOrEmpty(): String {
    return (data.value as? ViewState.Success)?.profileUiState?.member?.phoneNumber ?: ""
  }

  fun phoneNumberChanged(newPhoneNumber: String) {
    if (currentPhoneNumberOrEmpty() != newPhoneNumber && dirty.value != true) {
      dirty.value = true
    }
  }

  fun onLogout() {
    logoutUseCase.invoke()
  }
}

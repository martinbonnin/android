package com.hedvig.app.feature.trustly

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

abstract class TrustlyViewModel : ViewModel() {
  protected val _data = MutableLiveData<String>()
  val data: LiveData<String> = _data
}

class TrustlyViewModelImpl(
  private val repository: TrustlyRepository,
  hAnalytics: HAnalytics,
) : TrustlyViewModel() {
  init {
    hAnalytics.screenView(AppScreen.CONNECT_PAYMENT_TRUSTLY)
    viewModelScope.launch {
      val response = runCatching { repository.startTrustlySession() }
      if (response.isFailure) {
        response.exceptionOrNull()?.let { logcat(LogPriority.ERROR, it) { "Trustly session failed to start" } }
        return@launch
      }
      response.getOrNull()?.data?.startDirectDebitRegistration?.let { _data.postValue(it) }
    }
  }
}

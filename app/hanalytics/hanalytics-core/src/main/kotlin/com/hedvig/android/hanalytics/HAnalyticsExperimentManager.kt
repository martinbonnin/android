package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal interface HAnalyticsExperimentManager {
  suspend fun getExperiment(name: String): HAnalyticsExperiment
  suspend fun invalidateExperiments()
}

internal class HAnalyticsExperimentManagerImpl(
  private val sendHAnalyticsEventUseCase: SendHAnalyticsEventUseCase,
  private val hAnalyticsService: HAnalyticsService,
) : HAnalyticsExperimentManager {
  private val mutex = Mutex()
  private val experimentsData = mutableMapOf<String, String>()

  /**
   * Throwing an exception is a normal behavior, which will result in the experiment returning the default value.
   */
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    return mutex.withLock {
      if (experimentsData.isEmpty()) {
        loadExperimentsFromServer()
      }

      experimentsData[name]?.let { variant ->
        HAnalyticsExperiment(name, variant)
      } ?: error("experiment unavailable")
    }
  }

  override suspend fun invalidateExperiments() {
    mutex.withLock {
      experimentsData.clear()
    }
  }

  private suspend fun loadExperimentsFromServer() {
    val experimentsList = hAnalyticsService.getExperiments()
    sendExperimentsLoadedEvent(experimentsList)
    if (experimentsList == null) return
    experimentsData.clear()
    experimentsData.putAll(experimentsList.map { it.name to it.variant })
  }

  private fun sendExperimentsLoadedEvent(experimentsList: List<Experiment>?) {
    val experimentsLoadedEvent = HAnalyticsEvent(
      "experiments_loaded",
      mapOf("experiments" to experimentsList),
    )
    sendHAnalyticsEventUseCase.send(experimentsLoadedEvent)
  }
}

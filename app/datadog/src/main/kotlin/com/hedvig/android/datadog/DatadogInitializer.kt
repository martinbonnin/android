package com.hedvig.android.datadog

import android.content.Context
import androidx.startup.Initializer
import com.datadog.android.Datadog
import com.datadog.android.DatadogSite
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy
import com.datadog.android.tracing.AndroidTracer
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.opentracing.util.GlobalTracer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

// Used in /app/src/main/AndroidManifest.xml
abstract class DatadogInitializer : Initializer<Unit>, KoinComponent {

  private val hedvigBuildConstants by inject<HedvigBuildConstants>()
  private val deviceIdDataStore by inject<DeviceIdDataStore>()
  private val applicationScope by inject<ApplicationScope>()

  override fun create(context: Context) {
    val clientToken = "pub185bcba7ed324e83d068b80e25a81359"
    val applicationId = "4d7b8355-396d-406e-b543-30a073050e8f"

    val environmentName = if (hedvigBuildConstants.isProduction) "prod" else "dev"
    val configuration = Configuration.Builder(
      logsEnabled = true,
      tracesEnabled = true,
      crashReportsEnabled = true,
      rumEnabled = true,
    )
      .useSite(DatadogSite.EU1)
      .trackInteractions()
      .trackLongTasks(300)
      .setFirstPartyHosts(listOf("app.datadoghq.eu"))
      .useViewTrackingStrategy(ActivityViewTrackingStrategy(true))
      .build()

    val credentials = Credentials(
      clientToken = clientToken,
      envName = environmentName,
      variant = Credentials.NO_VARIANT,
      rumApplicationId = applicationId,
      serviceName = "android",
    )
    if (hedvigBuildConstants.isDebug) {
      Datadog.setVerbosity(android.util.Log.VERBOSE)
    }
    Datadog.initialize(context, credentials, configuration, TrackingConsent.GRANTED)
    val didRegisterGlobalRum = GlobalRum.registerIfAbsent {
      RumMonitor.Builder().build()
    }
    logcat(LogPriority.VERBOSE) { "Datadog RUM registering succeeded: $didRegisterGlobalRum" }
    val didRegisterGlobalTracer = GlobalTracer.registerIfAbsent {
      AndroidTracer.Builder().build()
    }
    logcat(LogPriority.VERBOSE) { "Datadog Global Tracer registering succeeded: $didRegisterGlobalTracer" }
    applicationScope.launch {
      val deviceId = deviceIdDataStore.observeDeviceId().first()
      Datadog.addUserExtraInfo(mapOf(DEVICE_ID_KEY to deviceId))
      GlobalRum.addAttribute(DEVICE_ID_KEY, deviceId)
    }

    Timber.plant(DatadogLoggingTree())
  }
}

private const val DEVICE_ID_KEY = "device_id"

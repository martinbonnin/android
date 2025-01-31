package com.hedvig.android.feature.travelcertificate.data

import android.content.Context
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.await
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter

private const val CERTIFICATE_NAME = "hedvigTravelCertificate_"
private const val FILE_EXT = ".pdf"

internal class DownloadTravelCertificateUseCase(
  private val context: Context,
) {

  suspend fun invoke(travelCertificateUri: TravelCertificateUrl): Either<ErrorMessage, TravelCertificateUri> =
    withContext(Dispatchers.IO) {
      either {
        val request = Request.Builder()
          .url(travelCertificateUri.uri)
          .build()

        val now = DateTimeFormatter.ISO_DATE_TIME.format(
          Clock.System.now()
            .toLocalDateTime(TimeZone.UTC)
            .toJavaLocalDateTime(),
        )

        val downloadedFile = File(context.filesDir, CERTIFICATE_NAME + now + FILE_EXT)

        try {
          val response = OkHttpClient().newCall(request).await()
          val buffer = downloadedFile.sink().buffer()
          buffer.writeAll(response.body!!.source())
          buffer.close()

          TravelCertificateUri(downloadedFile)
        } catch (exception: IOException) {
          logcat(LogPriority.ERROR, exception) { "Could not download travel certificate" }
          raise(ErrorMessage("Could not download travel certificate"))
        }
      }
    }
}

@JvmInline
internal value class TravelCertificateUri(val uri: File)

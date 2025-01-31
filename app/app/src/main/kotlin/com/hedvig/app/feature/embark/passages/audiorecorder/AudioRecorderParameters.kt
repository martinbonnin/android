package com.hedvig.app.feature.embark.passages.audiorecorder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioRecorderParameters(
  val messages: List<String>,
  val key: String,
  val label: String,
  val link: String,
) : Parcelable

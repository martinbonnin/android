package com.hedvig.app.feature.perils

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import giraffe.fragment.PerilFragmentV2
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class Peril(
  val title: String,
  val description: String,
  val darkUrl: String?,
  val lightUrl: String?,
  val exception: List<String>,
  val covered: List<String>,
  val info: String,
) : Parcelable {
  companion object {
    fun from(fragment: PerilFragmentV2) = Peril(
      title = fragment.title,
      description = fragment.description,
      darkUrl = fragment.icon.variants.dark.svgUrl,
      lightUrl = fragment.icon.variants.light.svgUrl,
      exception = fragment.exceptions,
      covered = fragment.covered,
      info = fragment.info,
    )
  }
}

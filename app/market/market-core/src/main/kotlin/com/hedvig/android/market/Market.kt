package com.hedvig.android.market

import android.net.Uri
import giraffe.type.Locale

enum class Market {
  SE,
  NO,
  DK,
  FR,
  ;

  val flag: Int
    get() = when (this) {
      SE -> hedvig.resources.R.drawable.ic_flag_se
      NO -> hedvig.resources.R.drawable.ic_flag_no
      DK -> hedvig.resources.R.drawable.ic_flag_dk
      FR -> hedvig.resources.R.drawable.ic_flag_fr
    }

  val label: Int
    get() = when (this) {
      SE -> hedvig.resources.R.string.market_sweden
      NO -> hedvig.resources.R.string.market_norway
      DK -> hedvig.resources.R.string.market_denmark
      FR -> hedvig.resources.R.string.market_france
    }

  fun defaultLanguage() = when (this) {
    SE -> Language.EN_SE
    NO -> Language.EN_NO
    DK -> Language.EN_DK
    FR -> Language.EN_FR
  }

  companion object {
    const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
  }
}

fun Market.createOnboardingUri(baseUrl: String, language: Language): Uri {
  val webPath = language.webPath()
  val builder = Uri.Builder()
    .scheme("https")
    .authority(baseUrl)
    .appendPath(webPath)
    .appendPath(
      when (language.toLocale()) {
        Locale.sv_SE -> "forsakringar"
        Locale.en_SE,
        Locale.nb_NO,
        Locale.en_NO,
        Locale.da_DK,
        Locale.en_DK,
        Locale.UNKNOWN__,
        -> "insurances"
      },
    )
    .appendQueryParameter("utm_source", "android")
    .appendQueryParameter("utm_medium", "hedvig-app")

  if (this == Market.SE) {
    builder.appendQueryParameter("utm_campaign", "se")
  }

  return builder.build()
}

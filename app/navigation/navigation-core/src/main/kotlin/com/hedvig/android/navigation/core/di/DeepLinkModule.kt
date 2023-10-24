package com.hedvig.android.navigation.core.di

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.HedvigDeepLinkContainerImpl
import org.koin.dsl.module

val deepLinkModule = module {
  single<HedvigDeepLinkContainer> {
    val hedvigBuildConstants = get<HedvigBuildConstants>()
    HedvigDeepLinkContainerImpl(
      isProduction = hedvigBuildConstants.isProduction,
      isDev = !hedvigBuildConstants.isProduction && hedvigBuildConstants.isDebug,
    )
  }
}

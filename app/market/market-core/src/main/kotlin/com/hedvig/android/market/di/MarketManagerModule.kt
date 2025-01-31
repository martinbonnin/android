package com.hedvig.android.market.di

import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.MarketManagerImpl
import org.koin.dsl.module

val marketManagerModule = module {
  single<MarketManager> { MarketManagerImpl(get(), isDebug = get<HedvigBuildConstants>().isDebug) }
}

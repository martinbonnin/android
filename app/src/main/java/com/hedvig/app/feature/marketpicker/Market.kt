package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.hedvig.app.feature.adyen.AdyenConnectPayinActivity
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import com.hedvig.app.R

enum class Market {
    SE,
    NO;

    fun connectPayin(context: Context) = when (this) {
        SE -> TrustlyConnectPayinActivity.newInstance(
            context
        )
        NO -> AdyenConnectPayinActivity.newInstance(
            context,
            AdyenCurrency.fromMarket(this)
        )
    }

    fun getFlag() = when (this) {
        SE -> R.drawable.ic_flag_se
        NO -> R.drawable.ic_flag_no
    }

    companion object {
        const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    }
}

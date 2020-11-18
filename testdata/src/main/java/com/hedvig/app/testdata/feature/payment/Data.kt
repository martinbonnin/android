package com.hedvig.app.testdata.feature.payment

import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.util.months
import java.time.LocalDate

val PAYIN_STATUS_DATA_NEEDS_SETUP = PayinStatusQuery.Data(PayinMethodStatus.NEEDS_SETUP)
val PAYIN_STATUS_DATA_ACTIVE = PayinStatusQuery.Data(PayinMethodStatus.ACTIVE)
val PAYIN_STATUS_DATA_PENDING = PayinStatusQuery.Data(PayinMethodStatus.PENDING)

val PAYMENT_DATA_NOT_CONNECTED = PaymentDataBuilder().build()
val PAYMENT_DATA_FAILED_PAYMENTS = PaymentDataBuilder(failedCharges = 1).build()
val PAYMENT_DATA_HISTORIC_PAYMENTS = PaymentDataBuilder(
    chargeHistory = listOf(
        ChargeHistoryBuilder().build(),
        ChargeHistoryBuilder(date = LocalDate.now() - 2.months).build(),
    )
).build()
val PAYMENT_DATA_TRUSTLY_CONNECTED = PaymentDataBuilder(
    payinType = PayinType.TRUSTLY,
    payinConnected = true,
).build()
val PAYMENT_DATA_ADYEN_CONNECTED = PaymentDataBuilder(
    payinType = PayinType.ADYEN,
    payinConnected = true,
).build()
val PAYMENT_DATA_FREE_MONTHS = PaymentDataBuilder(
    freeUntil = LocalDate.now() + 3.months,
    redeemedCampaigns = listOf(
        PaymentQuery.RedeemedCampaign(
            owner = PaymentQuery.Owner(
                displayName = "Test Owner"
            ),
            fragments = PaymentQuery.RedeemedCampaign.Fragments(
                IncentiveFragment(
                    incentive = IncentiveFragment.Incentive(
                        asFreeMonths = IncentiveFragment.AsFreeMonths(
                            quantity = 3
                        ),
                        asMonthlyCostDeduction = null,
                        asNoDiscount = null,
                        asPercentageDiscountMonths = null,
                    )
                )
            )
        )
    )
).build()

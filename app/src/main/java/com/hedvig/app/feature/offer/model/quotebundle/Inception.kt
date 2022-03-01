package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationStartDateTerminology
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData

data class Inception(
    val startDate: OfferStartDate,
    val startDateLabel: StartDateLabel,
    val changeDateData: ChangeDateBottomSheetData
)

fun QuoteBundleFragment.Inception1.toInception(
    startDateTerminology: QuoteBundleAppConfigurationStartDateTerminology
) = Inception(
    startDate = getStartDate(),
    startDateLabel = getStartDateLabel(startDateTerminology),
    changeDateData = toChangeDateBottomSheetData()
)

package com.hedvig.app.testdata.feature.referrals.builders

import giraffe.LoggedInQuery
import giraffe.fragment.MonetaryAmountFragment
import giraffe.type.MonthlyCostDeduction

data class LoggedInDataBuilder(
  val referralTermsUrl: String = "https://www.example.com",
  val campaignIncentiveAmount: String = "10.00",
  val campaignIncentiveCurrency: String = "SEK",
) {
  fun build() = LoggedInQuery.Data(
    referralTerms = LoggedInQuery.ReferralTerms(
      url = referralTermsUrl,
    ),
    referralInformation = LoggedInQuery.ReferralInformation(
      campaign = LoggedInQuery.Campaign(
        incentive = LoggedInQuery.Incentive(
          __typename = MonthlyCostDeduction.type.name,
          asMonthlyCostDeduction = LoggedInQuery.AsMonthlyCostDeduction(
            __typename = MonthlyCostDeduction.type.name,
            amount = LoggedInQuery.Amount(
              __typename = "",
              fragments = LoggedInQuery.Amount.Fragments(
                MonetaryAmountFragment(
                  amount = campaignIncentiveAmount,
                  currency = campaignIncentiveCurrency,
                ),
              ),
            ),
          ),
        ),
      ),
    ),
  )
}

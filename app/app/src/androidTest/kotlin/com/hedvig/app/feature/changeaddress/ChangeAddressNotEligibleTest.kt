package com.hedvig.app.feature.changeaddress

import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.testdata.feature.changeaddress.BLOCKED_SELF_CHANGE_ELIGIBILITY
import com.hedvig.app.testdata.feature.changeaddress.UPCOMING_AGREEMENT_NONE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import giraffe.ActiveContractBundlesQuery
import giraffe.UpcomingAgreementQuery
import org.junit.Rule
import org.junit.Test

class ChangeAddressNotEligibleTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(ChangeAddressActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    UpcomingAgreementQuery.OPERATION_DOCUMENT to apolloResponse { success(UPCOMING_AGREEMENT_NONE) },
    ActiveContractBundlesQuery.OPERATION_DOCUMENT to apolloResponse { success(BLOCKED_SELF_CHANGE_ELIGIBILITY) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldShowManualChangeAddressWhenEligibilityIsBlocked() = run {
    activityRule.launch(ChangeAddressActivity.newInstance(context()))

    ChangeAddressScreen {
      title {
        hasText(hedvig.resources.R.string.moving_intro_title)
      }

      subtitle {
        hasText(hedvig.resources.R.string.moving_intro_manual_handling_description)
      }

      continueButton {
        hasText(hedvig.resources.R.string.moving_intro_manual_handling_button_text)
      }
    }
  }
}

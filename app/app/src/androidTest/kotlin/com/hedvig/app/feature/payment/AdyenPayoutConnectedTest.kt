package com.hedvig.app.feature.payment

import com.hedvig.android.market.Market
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_PAYOUT_CONNECTED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import giraffe.PayinStatusQuery
import giraffe.PaymentQuery
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class AdyenPayoutConnectedTest : TestCase() {

  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(PaymentActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    PaymentQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYMENT_DATA_PAYOUT_CONNECTED) },
    PayinStatusQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @get:Rule
  val marketRule = MarketRule(Market.NO)

  @Test
  fun shouldShowConnectPayoutWhenInNorwayAndPayoutIsConnected() = run {
    activityRule.launch(PaymentActivity.newInstance(context()))

    onScreen<PaymentScreen> {
      adyenConnectPayout { stub() }
      recycler {
        childAt<PaymentScreen.AdyenPayoutDetails>(4) {
          status {
            hasText(hedvig.resources.R.string.payment_screen_pay_connected_label)
          }
        }
        childAt<PaymentScreen.AdyenPayoutParagraph>(5) {
          text { hasText(hedvig.resources.R.string.payment_screen_pay_out_connected_payout_footer_connected) }
        }
        childAt<PaymentScreen.Link>(6) {
          click()
        }
      }
      adyenConnectPayout { intended() }
    }
  }
}

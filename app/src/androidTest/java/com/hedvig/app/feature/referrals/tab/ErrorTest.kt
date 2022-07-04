package com.hedvig.app.feature.referrals.tab

import com.hedvig.android.core.jsonObjectOf
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.FeatureFlagRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.featureflags.flags.Feature
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class ErrorTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    var shouldFail = true

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        LoggedInQuery.OPERATION_DOCUMENT to apolloResponse { success(LOGGED_IN_DATA) },
        ReferralsQuery.OPERATION_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError(jsonObjectOf("message" to "example message"))
            } else {
                success(REFERRALS_DATA_WITH_NO_DISCOUNTS)
            }
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @get:Rule
    val featureFlagRule = FeatureFlagRule(
        Feature.REFERRAL_CAMPAIGN to false,
        Feature.KEY_GEAR to false,
        Feature.REFERRALS to true,
    )

    @Test
    fun shouldShowErrorWhenAnErrorOccurs() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launch(intent)

        onScreen<ReferralTabScreen> {
            share { isGone() }
            recycler {
                hasSize(2)
                childAt<ReferralTabScreen.ErrorItem>(1) {
                    errorTitle { isVisible() }
                    errorParagraph { isVisible() }
                    retry {
                        isVisible()
                        click()
                    }
                }
                hasSize(3)
                childAt<ReferralTabScreen.HeaderItem>(1) {
                    discountPerMonthPlaceholder { isGone() }
                    newPricePlaceholder { isGone() }
                    discountPerMonth { isGone() }
                    newPrice { isGone() }
                    discountPerMonthLabel { isGone() }
                    newPriceLabel { isGone() }
                    emptyHeadline { isVisible() }
                    emptyBody { isVisible() }
                    otherDiscountBox { isGone() }
                }
                childAt<ReferralTabScreen.CodeItem>(2) {
                    placeholder { isGone() }
                    code {
                        isVisible()
                        hasText("TEST123")
                    }
                }
            }
        }
    }
}

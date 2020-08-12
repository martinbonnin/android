package com.hedvig.app.feature.home

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RunWith(AndroidJUnit4::class)
class ActiveInFutureAndTerminatedInFutureTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.OPERATION_NAME to { LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED },
        HomeQuery.OPERATION_NAME to { HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowMessageWhenUserHasAllContractsInActiveInFutureStateOrActiveInFutureAndTerminatedInFutureState() {
        activityRule.launchActivity(LoggedInActivity.newInstance(ApplicationProvider.getApplicationContext()))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.BigTextItem>(0) {
                    text { hasText("Test ${formatter.format(LocalDate.of(2024, 1, 1))} TODO") }
                }
            }
        }
    }
}


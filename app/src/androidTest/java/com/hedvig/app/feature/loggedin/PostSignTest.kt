package com.hedvig.app.feature.loggedin

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity.Companion.EXTRA_IS_FROM_ONBOARDING
import com.hedvig.app.testdata.feature.loggedin.WELCOME_DATA_ONE_PAGE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostSignTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        WelcomeQuery.QUERY_DOCUMENT to apolloResponse {
            success(WELCOME_DATA_ONE_PAGE)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenWelcomeWhenNavigatingFromOnboarding() {
        activityRule.launchActivity(
            LoggedInActivity.newInstance(context())
                .apply { putExtra(EXTRA_IS_FROM_ONBOARDING, true) })

        onScreen<WelcomeScreen> {
            close {
                isVisible()
                click()
            }
        }
        onScreen<LoggedInScreen> {
            pressBack()
            root { isVisible() }
            bottomTabs {
                hasSelectedItem(R.id.home)
            }
        }
    }
}


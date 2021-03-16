package com.hedvig.onboarding.externalredirect

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_EXTERNAL_REDIRECT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.onboarding.screens.EmbarkScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class OfferTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_EXTERNAL_REDIRECT) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenWebOfferWhenEncounteringExternalRedirect() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this::class.java.name))

        onScreen<EmbarkScreen> {
            offer { stub() }
            selectActions { childAt<EmbarkScreen.SelectAction>(0) { click() } }
            flakySafely { offer { intended() } }
        }
    }
}

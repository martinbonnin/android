package com.hedvig.app.feature.embark.api.graphqlquery

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.hedvig.android.core.common.android.jsonObjectOf
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.HELLO_QUERY
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import giraffe.EmbarkStoryQuery
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class SuccessTest : TestCase() {
  @get:Rule
  val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

  @get:Rule
  val compose = createComposeRule()

  @get:Rule
  val apolloMockServerRule = ApolloMockServerRule(
    EmbarkStoryQuery.OPERATION_DOCUMENT to apolloResponse { success(STORY_WITH_GRAPHQL_QUERY_API) },
    HELLO_QUERY to apolloResponse {
      success(jsonObjectOf("hello" to "world"))
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldRedirectAndSaveResultsWhenLoadingPassageWithGraphQLQueryApiThatIsSuccessful() = run {
    activityRule.launch(
      EmbarkActivity.newInstance(
        context(),
        this.javaClass.name,
        "",
      ),
    )

    onScreen<EmbarkScreen> {
      compose
        .onNodeWithTag("selectActionGrid")
        .onChildren()
        .onFirst()
        .performClick()
      messages {
        hasSize(1)
        firstChild<EmbarkScreen.MessageRow> {
          text { hasText("api result: world") }
        }
      }
    }
  }
}

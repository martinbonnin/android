package com.hedvig.app.feature.embark.textaction

import com.hedvig.android.core.common.android.jsonObjectOf
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.TextActionSetScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.HELLO_QUERY
import com.hedvig.app.testdata.feature.embark.data.STANDARD_THIRD_MESSAGE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_API
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

class TextActionApiTest : TestCase() {
  @get:Rule
  val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

  @get:Rule
  val apolloMockServerRule = ApolloMockServerRule(
    EmbarkStoryQuery.OPERATION_DOCUMENT to apolloResponse { success(STORY_WITH_TEXT_ACTION_API) },
    HELLO_QUERY to apolloResponse {
      success(jsonObjectOf("hello" to "world"))
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun whenSubmittingTextActionWithApiShouldCallApi() = run {
    activityRule.launch(EmbarkActivity.newInstance(context(), "", ""))

    step("Input something into field and submit") {
      TextActionSetScreen {
        input(0) {
          edit { typeText("test") }
        }
        submit { click() }
      }
    }

    step("Verify that success-passage from API is redirected to") {
      onScreen<EmbarkScreen> {
        messages {
          childAt<EmbarkScreen.MessageRow>(0) { text { hasText(STANDARD_THIRD_MESSAGE.text) } }
        }
      }
    }
  }
}

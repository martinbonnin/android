package com.hedvig.app.feature.home.screens

import android.content.Intent
import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.connect.ConnectPaymentActivity
import org.hamcrest.Matcher

class HomeTabScreen : Screen<HomeTabScreen>() {
    val recycler =
        KRecyclerView({ withId(R.id.recycler) },
            {
                itemType(::BigTextItem)
                itemType(::BodyTextItem)
                itemType(::StartClaimItem)
                itemType(::InfoCardItem)
                itemType(::CommonClaimTitleItem)
                itemType(::CommonClaimItem)
                itemType(::ErrorItem)
            })

    class BigTextItem(parent: Matcher<View>) : KRecyclerItem<BigTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class BodyTextItem(parent: Matcher<View>) : KRecyclerItem<BodyTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class StartClaimItem(parent: Matcher<View>) : KRecyclerItem<StartClaimItem>(parent) {
        val button = KButton { withMatcher(parent) }
    }

    class InfoCardItem(parent: Matcher<View>) : KRecyclerItem<InfoCardItem>(parent) {
        val title = KTextView(parent) { withId(R.id.title) }
        val body = KTextView(parent) { withId(R.id.body) }
        val action = KButton(parent) { withId(R.id.action) }

        val connectPayin = KIntent {
            hasComponent(ConnectPaymentActivity::class.java.name)
        }

        val psaLink = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }
    }

    class CommonClaimTitleItem(parent: Matcher<View>) :
        KRecyclerItem<CommonClaimTitleItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class CommonClaimItem(parent: Matcher<View>) : KRecyclerItem<CommonClaimItem>(parent) {
        val text = KTextView(parent) { withId(R.id.label) }
    }

    class ErrorItem(parent: Matcher<View>) : KRecyclerItem<ErrorItem>(parent) {
        val retry = KButton(parent) { withId(R.id.retry) }
    }
}

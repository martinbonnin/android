package com.hedvig.app.feature.referrals

import android.view.View
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import org.hamcrest.Matcher

class ReferralScreen : Screen<ReferralScreen>() {
    val share = KButton { withId(R.id.share) }
    val recycler = KRecyclerView({ withId(R.id.invites) }, itemTypeBuilder = {
        itemType(::HeaderItem)
        itemType(::CodeItem)
        itemType(::ReferralItem)
    })

    class HeaderItem(parent: Matcher<View>) : KRecyclerItem<HeaderItem>(parent) {
        val discountPerMonthPlaceholder =
            KImageView(parent) { withId(R.id.discountPerMonthPlaceholder) }
        val newPricePlaceholder = KImageView(parent) { withId(R.id.newPricePlaceholder) }

        val discountPerMonth = KTextView(parent) { withId(R.id.discountPerMonth) }
        val newPrice = KTextView(parent) { withId(R.id.newPrice) }
    }

    class CodeItem(parent: Matcher<View>) : KRecyclerItem<CodeItem>(parent) {
        val placeholder = KImageView(parent) { withId(R.id.codePlaceholder) }
        val code = KTextView(parent) { withId(R.id.code) }
    }

    class ReferralItem(parent: Matcher<View>) : KRecyclerItem<ReferralItem>(parent) {
        val iconPlaceholder = KImageView(parent) { withId(R.id.iconPlaceholder) }
        val textPlaceholder = KImageView(parent) { withId(R.id.textPlaceholder) }
        val icon = KImageView(parent) { withId(R.id.icon) }
    }
}

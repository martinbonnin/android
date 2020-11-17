package com.hedvig.app.feature.payment

import android.view.View
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.PaymentHistoryActivity
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import org.hamcrest.Matcher

class PaymentScreen : Screen<PaymentScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) }, {
        itemType(::FailedPayments)
        itemType(::ConnectPayin)
        itemType(::Charge)
        itemType(::PaymentHistoryLink)
        itemType(::TrustlyPayinDetails)
        itemType(::AdyenPayinDetails)
    })

    class FailedPayments(parent: Matcher<View>) : KRecyclerItem<FailedPayments>(parent) {
        val paragraph = KTextView(parent) { withId(R.id.failedPaymentsParagraph) }
    }

    class ConnectPayin(parent: Matcher<View>) : KRecyclerItem<ConnectPayin>(parent) {
        val connect = KButton(parent) { withId(R.id.connectBankAccount) }
    }

    class Charge(parent: Matcher<View>) : KRecyclerItem<Charge>(parent) {
        val amount = KTextView(parent) { withId(R.id.amount) }
        val date = KTextView(parent) { withId(R.id.date) }
    }

    class PaymentHistoryLink(parent: Matcher<View>) : KRecyclerItem<PaymentHistoryLink>(parent)

    class TrustlyPayinDetails(parent: Matcher<View>) : KRecyclerItem<TrustlyPayinDetails>(parent) {
        val accountNumber = KTextView(parent) { withId(R.id.accountNumber) }
        val status = KTextView(parent) { withId(R.id.directDebitStatus) }
    }

    class AdyenPayinDetails(parent: Matcher<View>) : KRecyclerItem<AdyenPayinDetails>(parent) {
        val cardType = KTextView(parent) { withId(R.id.cardType) }
        val maskedCardNumber = KTextView(parent) { withId(R.id.maskedCardNumber) }
        val validUntil = KTextView(parent) { withId(R.id.validUntil) }
    }

    val trustlyConnectPayin = KIntent { hasComponent(TrustlyConnectPayinActivity::class.java.name) }
    val paymentHistory = KIntent { hasComponent(PaymentHistoryActivity::class.java.name) }
}

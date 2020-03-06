package com.hedvig.app.feature.welcome

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.dismissablepager.DismissablePager
import com.hedvig.app.feature.dismissablepager.DismissablePagerPage
import com.hedvig.app.feature.ratings.RatingsDialog
import org.koin.android.ext.android.inject

class WelcomeDialog : DismissablePager() {

    override val proceedLabel = R.string.NEWS_PROCEED
    override val dismissLabel = R.string.NEWS_DISMISS
    override val animationStyle = R.style.WelcomeDialogAnimation
    override val titleLabel: Nothing? = null

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissablePagerPage> by lazy {
        arguments?.getParcelableArrayList<DismissablePagerPage>(ITEMS)
            ?: throw Error("Cannot create a WelcomeDialog without any items")
    }

    override fun onDismiss() {
        RatingsDialog
            .newInstance()
            .show(parentFragmentManager, RatingsDialog.TAG)
    }

    companion object {
        const val TAG = "WelcomeDialog"
        private const val ITEMS = "items"

        fun newInstance(items: List<DismissablePagerPage>) = WelcomeDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ITEMS, ArrayList(items))
            }
        }
    }
}

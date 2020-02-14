package com.hedvig.app

import android.net.Uri
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.util.LiveEvent
import org.threeten.bp.YearMonth

class MockKeyGearItemDetailViewModel : KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override val isUploading = LiveEvent<Boolean>()

    override fun loadItem(id: String) {
        Handler().postDelayed({
            data.postValue(
                KeyGearItemQuery.KeyGearItem(
                    "KeyGearItem",
                    KeyGearItemQuery.KeyGearItem.Fragments(items[id]!!)
                )
            )
        }, 250)
    }

    override fun uploadReceipt(uri: Uri) {
        val id = data.value?.fragments?.keyGearItemFragment?.id ?: return
        isUploading.value = true
        Handler().postDelayed({
            data.postValue(
                KeyGearItemQuery.KeyGearItem(
                    "KeyGearItem",
                    KeyGearItemQuery.KeyGearItem.Fragments(
                        items[id]!!.toBuilder().receipts(
                            listOf(
                                KeyGearItemFragment.Receipt(
                                    "KeyGearItemReceipt",
                                    KeyGearItemFragment.File1(
                                        "S3File",
                                        "https://upload.wikimedia.org/wikipedia/commons/0/0b/ReceiptSwiss.jpg"
                                    )
                                )
                            )
                        ).build()
                    )
                )
            )
            isUploading.postValue(false)
        }, 2000)
    }

    override fun updatePurchaseDateAndPrice(
        id: String,
        yearMonth: YearMonth,
        price: MonetaryAmountV2Input
    ) {
        TODO()
    }

    companion object {
        val items = hashMapOf(
            "123" to
                KeyGearItemFragment(
                    "KeyGearItem",
                    "123",
                    listOf(
                        KeyGearItemFragment.Photo(
                            "KeyGearItemPhoto",
                            KeyGearItemFragment.File(
                                "S3File",
                                "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"
                            )
                        )
                    ),
                    listOf(),
                    KeyGearItemCategory.PHONE,
                    null,
                    null
                ),
            "234" to
                KeyGearItemFragment(
                    "KeyGearItem",
                    "234",
                    listOf(
                        KeyGearItemFragment.Photo(
                            "KeyGearItemPhoto",
                            KeyGearItemFragment.File(
                                "S3File",
                                "https://images.unsplash.com/photo-1522199755839-a2bacb67c546"
                            )
                        )
                    ),
                    listOf(),
                    KeyGearItemCategory.COMPUTER,
                    null,
                    null
                )
        )
    }
}

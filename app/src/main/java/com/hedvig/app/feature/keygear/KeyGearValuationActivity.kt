package com.hedvig.app.feature.keygear

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.android.owldroid.fragment.KeyGearItemValuationFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.ValuationData
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.safeLet
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.activity_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import java.text.DateFormatSymbols
import java.util.*

class KeyGearValuationActivity : BaseActivity(R.layout.activity_key_gear_valuation) {
    private val model: KeyGearValuationViewModel by viewModel()

    private var isUploading = false
    private lateinit var id: String
    private var date: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra(ITEM_ID)
        var maxInsurableAmount = 0

        saveContainer.show()
        model.data.observe(this) { data ->
            safeLet(
                data,
                data?.fragments?.keyGearItemFragment?.maxInsurableAmount?.amount
            ) { d, amout ->
                maxInsurableAmount = amout.toBigDecimal().toInt()
                val category =
                    resources.getString(d.fragments.keyGearItemFragment.category.label)
                        .toLowerCase()
                noCoverage.text = interpolateTextKey(
                    getString(R.string.KEY_GEAR_NOT_COVERED),
                    "ITEM_TYPE" to category
                )
                body.text = interpolateTextKey(
                    getString(R.string.KEY_GEAR_ITEM_VIEW_ADD_PURCHASE_DATE_BODY),
                    "ITEM_TYPE" to category
                )

            }
        }
        model.loadItem(id)


        dateInput.setHapticClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    date = LocalDate.of(year, month + 1, dayOfMonth)

                    val monthText = DateFormatSymbols().months[month]
                    dateInput.text = "$dayOfMonth $monthText $year"

                    setButtonState(priceInput.getText().isNotEmpty(), date != null)
                },
                date?.year ?: calendar.get(Calendar.YEAR),
                date?.monthValue ?: calendar.get(Calendar.MONTH),
                date?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = calendar.time.time
                show()
            }
        }

        close.setHapticClickListener {
            onBackPressed()
        }

        save.setHapticClickListener {
            if (isUploading) {
                return@setHapticClickListener
            }
            isUploading = true
            transitionToUploading()

            val price = priceInput.getText()
            safeLet(date, id) { date, id ->
                val monetaryValue =
                    MonetaryAmountV2Input.builder().amount(price).currency("SEK").build()

                model.updatePurchaseDateAndPrice(id, date, monetaryValue)
            }
        }

        priceInput.setOnChangeListener {
            val text = priceInput.getText()
            setButtonState(text.isNotEmpty(), date != null)
            if (!text.isNullOrBlank()) {
                try {
                    val value = text.toDouble()
                    if (value > maxInsurableAmount.toDouble()) {
                        animateDateDown()
                        noCoverage.show()
                    } else {
                        animateDateUp()
                        noCoverage.remove()
                    }
                } catch (e: Exception) {
                }
            }
        }

        model.uploadResult.observe(this) { uploadResult ->
            safeLet(uploadResult?.keyGearItem, uploadResult?.keyGearItem?.fragments?.keyGearItemFragment?.purchasePrice?.amount) { item, amount ->
                val type = valuationType(item)
                if (type == ValuationType.FIXED) {
                    startActivity(
                        KeyGearValuationInfoActivity.newInstance(
                            this,
                            item.fragments.keyGearItemFragment.category,
                            ValuationData.from(
                                amount,
                                type,
                                (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation as KeyGearItemValuationFragment.AsKeyGearItemValuationFixed).ratio,
                                (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation as KeyGearItemValuationFragment.AsKeyGearItemValuationFixed).valuation.amount
                            )
                        )
                    )
                    finish()
                } else if (type == ValuationType.MARKET_PRICE) {
                    startActivity(
                        KeyGearValuationInfoActivity.newInstance(
                            this,
                            item.fragments.keyGearItemFragment.category,
                            ValuationData.from(
                                amount,
                                type,
                                (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation as KeyGearItemValuationFragment.AsKeyGearItemValuationMarketValue).ratio
                            )
                        )
                    )
                    finish()
                }
            }
        }
    }

    private fun animateDateDown() {
        dateInput.spring(
            SpringAnimation.TRANSLATION_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(getNoCoverageHeight())

        saveContainer.spring(
            SpringAnimation.TRANSLATION_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(getNoCoverageHeight())
    }

    private fun animateDateUp() {
        dateInput.spring(
            SpringAnimation.TRANSLATION_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(-(getNoCoverageHeight() / 50f))

        saveContainer.spring(
            SpringAnimation.TRANSLATION_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(-(getNoCoverageHeight() / 50f))
    }

    private fun getNoCoverageHeight(): Float {
        noCoverage.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        return noCoverage.measuredHeight.toFloat()
    }

    private fun valuationType(item: KeyGearItemQuery.KeyGearItem): ValuationType? {
        return when (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation) {
            is KeyGearItemValuationFragment.AsKeyGearItemValuationFixed -> ValuationType.FIXED
            is KeyGearItemValuationFragment.AsKeyGearItemValuationMarketValue -> ValuationType.MARKET_PRICE
            else -> null
        }
    }

    private fun transitionToUploading() {
        loadingIndicator.show()
        val startCornerRadius = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((saveContainer.background as RippleDrawable).getDrawable(0) as GradientDrawable).cornerRadius
        } else {
            BUTTON_CORNER_RADIUS
        }
        ValueAnimator.ofInt(saveContainer.width, saveContainer.height).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = SAVE_BUTTON_TRANSITION_DURATION
            addUpdateListener { va ->
                saveContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                    width = va.animatedValue as Int
                }
                save.alpha = 1 - va.animatedFraction
                loadingIndicator.alpha = va.animatedFraction
                val backgroundShape =
                    ((saveContainer.background as? RippleDrawable)?.getDrawable(0) as? GradientDrawable)?.mutate() as? GradientDrawable
                backgroundShape?.cornerRadius =
                    boundedLerp(startCornerRadius, saveContainer.height / 2f, va.animatedFraction)
            }
            start()
        }
    }

    private fun setButtonState(hasPrice: Boolean, hasDate: Boolean) {
        if (hasPrice && hasDate) {
            save.isEnabled = true
            saveContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.link_purple)
        } else {
            save.isEnabled = false
            saveContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.semi_light_gray)
        }
    }

    companion object {
        private const val ITEM_ID = "ITEM_ID"

        private val BUTTON_CORNER_RADIUS = 112.dp.toFloat()

        private const val SAVE_BUTTON_TRANSITION_DURATION = 200L

        fun newInstance(context: Context, id: String) =
            Intent(context, KeyGearValuationActivity::class.java).apply {
                putExtra(ITEM_ID, id)
            }
    }
}

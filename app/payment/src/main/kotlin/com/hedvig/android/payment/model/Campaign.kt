package com.hedvig.android.payment.model

import com.hedvig.android.apollo.toMonetaryAmount
import giraffe.fragment.IncentiveFragment
import giraffe.fragment.QuoteCartFragment
import org.javamoney.moneta.Money
import javax.money.MonetaryAmount

data class Campaign(
  val displayValue: String?,
  val incentive: Incentive,
  val code: String,
) {
  val shouldShowIncentive = incentive !is Incentive.NoDiscount

  sealed class Incentive {
    data class FreeMonths(
      val numberOfFreeMonths: Int,
    ) : Incentive()

    data class MonthlyCostDeduction(
      val amount: MonetaryAmount?,
    ) : Incentive()

    data class IndefinitePercentageDiscount(
      val percentage: Double,
    ) : Incentive()

    data class PercentageDiscountMonths(
      val percentage: Double,
      val numberOfMonths: Int,
    ) : Incentive()

    object NoDiscount : Incentive()

    object NoVisibleDiscount : Incentive()
  }
}

private fun QuoteCartFragment.Campaign.toCampaign() = Campaign(
  displayValue = displayValue,
  incentive = incentive?.toIncentive() ?: Campaign.Incentive.NoDiscount,
  code = "",
)

internal fun IncentiveFragment.Incentive?.toIncentive(): Campaign.Incentive {
  return this?.asFreeMonths?.let {
    Campaign.Incentive.FreeMonths(
      numberOfFreeMonths = it.quantity ?: 0,
    )
  } ?: this?.asMonthlyCostDeduction?.let {
    Campaign.Incentive.MonthlyCostDeduction(
      amount = Money.of(it.amount?.amount?.toBigDecimal(), "SEK"),
    )
  } ?: this?.asPercentageDiscountMonths?.let {
    Campaign.Incentive.PercentageDiscountMonths(
      percentage = it.percentageDiscount,
      numberOfMonths = it.pdmQuantity,
    )
  } ?: this?.asNoDiscount?.let {
    Campaign.Incentive.NoDiscount
  } ?: Campaign.Incentive.NoDiscount
}

private fun QuoteCartFragment.Incentive?.toIncentive(): Campaign.Incentive {
  return this?.asIndefinitePercentageDiscount?.let {
    Campaign.Incentive.IndefinitePercentageDiscount(
      percentage = it.indefinitePercentageDiscount,
    )
  } ?: this?.asFreeMonths?.let {
    Campaign.Incentive.FreeMonths(
      numberOfFreeMonths = it.freeQuantity ?: 0,
    )
  } ?: this?.asMonthlyCostDeduction?.let {
    Campaign.Incentive.MonthlyCostDeduction(
      amount = it.amount?.fragments?.monetaryAmountFragment?.toMonetaryAmount(),
    )
  } ?: this?.asPercentageDiscountMonths?.let {
    Campaign.Incentive.PercentageDiscountMonths(
      percentage = it.monthsPercentageDiscount,
      numberOfMonths = it.monthsQuantity,
    )
  } ?: this?.asNoDiscount?.let {
    Campaign.Incentive.NoDiscount
  } ?: this?.asVisibleNoDiscount?.let {
    Campaign.Incentive.NoVisibleDiscount
  } ?: Campaign.Incentive.NoDiscount
}

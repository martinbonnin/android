package com.hedvig.app.feature.offer.ui

import com.hedvig.app.feature.offer.model.CheckoutLabel
import giraffe.fragment.QuoteBundleFragment
import giraffe.type.CheckoutMethod
import giraffe.type.QuoteBundleAppConfigurationApproveButtonTerminology

fun QuoteBundleFragment.checkoutLabel(checkoutMethods: List<CheckoutMethod>) = when {
  checkoutMethods.contains(CheckoutMethod.SWEDISH_BANK_ID) -> CheckoutLabel.SIGN_UP
  checkoutMethods.contains(CheckoutMethod.SIMPLE_SIGN) -> CheckoutLabel.CONTINUE
  checkoutMethods.contains(CheckoutMethod.APPROVE_ONLY) -> when (appConfiguration.approveButtonTerminology) {
    QuoteBundleAppConfigurationApproveButtonTerminology.APPROVE_CHANGES -> CheckoutLabel.APPROVE
    QuoteBundleAppConfigurationApproveButtonTerminology.CONFIRM_PURCHASE -> CheckoutLabel.CONFIRM
    QuoteBundleAppConfigurationApproveButtonTerminology.UNKNOWN__ -> CheckoutLabel.UNKNOWN
  }
  else -> CheckoutLabel.UNKNOWN
}

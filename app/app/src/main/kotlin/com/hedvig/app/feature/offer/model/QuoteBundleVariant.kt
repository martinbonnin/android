package com.hedvig.app.feature.offer.model

import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.model.quotebundle.toQuoteBundle
import giraffe.fragment.QuoteCartFragment
import giraffe.type.CheckoutMethod

data class QuoteBundleVariant(
  val id: String,
  val title: String,
  val tag: String?,
  val description: String?,
  val bundle: QuoteBundle,
) {
  val externalProviderId = bundle
    .quotes
    .firstNotNullOfOrNull(QuoteBundle.Quote::dataCollectionId)
}

fun QuoteCartFragment.PossibleVariation.toQuoteBundleVariant(
  quoteCartId: QuoteCartId,
  checkoutMethods: List<CheckoutMethod>,
) = QuoteBundleVariant(
  id = id,
  title = bundle.fragments.quoteBundleFragment.displayName,
  tag = tag,
  description = description,
  bundle = bundle.fragments.quoteBundleFragment.toQuoteBundle(quoteCartId, checkoutMethods),
)

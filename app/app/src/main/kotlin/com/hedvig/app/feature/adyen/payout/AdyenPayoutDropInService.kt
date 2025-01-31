package com.hedvig.app.feature.adyen.payout

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.hedvig.android.payment.PaymentRepository
import com.hedvig.app.feature.adyen.ConnectPayoutUseCase
import com.hedvig.app.feature.adyen.SubmitAdditionalPaymentDetailsUseCase
import com.hedvig.app.feature.adyen.payin.toDropInServiceResult
import giraffe.type.PayoutMethodStatus
import giraffe.type.TokenizationResultType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class AdyenPayoutDropInService : DropInService(), CoroutineScope {
  private val paymentRepository: PaymentRepository by inject()
  private val submitAdditionalPaymentDetailsUseCase: SubmitAdditionalPaymentDetailsUseCase by inject()
  private val connectPayoutUseCase: ConnectPayoutUseCase by inject()

  private val coroutineJob = Job()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + coroutineJob

  override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
    launch(coroutineContext) {
      submitAdditionalPaymentDetailsUseCase.submitAdditionalPaymentDetails(actionComponentJson)
        .mapLeft { it.toDropInServiceResult() }
        .fold(
          ifLeft = { sendResult(it) },
          ifRight = {
            it.tokenizationResultType.toPayoutMethodStatusOrNull()
              ?.let { payoutMethodStatus ->
                runCatching { paymentRepository.writeActivePayoutMethodStatus(payoutMethodStatus) }
              }
            sendResult(DropInServiceResult.Finished(it.code))
          },
        )
    }
  }

  override fun onPaymentsCallRequested(
    paymentComponentState: PaymentComponentState<*>,
    paymentComponentJson: JSONObject,
  ) {
    launch(coroutineContext) {
      connectPayoutUseCase.connectPayout(paymentComponentJson)
        .mapLeft { it.toError() }
        .fold(
          ifLeft = { sendResult(it) },
          ifRight = {
            it.tokenizationResultType.toPayoutMethodStatusOrNull()?.let { payoutMethodStatus ->
              runCatching { paymentRepository.writeActivePayoutMethodStatus(payoutMethodStatus) }
            }
            sendResult(DropInServiceResult.Finished(it.code))
          },
        )
    }
  }

  private fun TokenizationResultType.toPayoutMethodStatusOrNull() = when (this) {
    TokenizationResultType.COMPLETED -> PayoutMethodStatus.ACTIVE
    TokenizationResultType.PENDING -> PayoutMethodStatus.PENDING
    else -> null
  }

  private fun ConnectPayoutUseCase.Error.toError() = when (this) {
    is ConnectPayoutUseCase.Error.CheckoutPaymentAction -> DropInServiceResult.Action(action)
    is ConnectPayoutUseCase.Error.ErrorMessage -> DropInServiceResult.Error(message)
  }
}

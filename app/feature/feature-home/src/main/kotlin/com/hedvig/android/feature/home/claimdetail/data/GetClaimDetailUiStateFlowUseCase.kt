package com.hedvig.android.feature.home.claimdetail.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailUiState
import giraffe.ClaimDetailsQuery
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

internal class GetClaimDetailUiStateFlowUseCase(
  private val getClaimDetailUseCase: GetClaimDetailUseCase,
) {
  /**
   * If the first invocation is a failure, simply returns the error and stops the flow.
   * If the first invocation is successful, starts polling the information, but does not stop or return the error.
   * This is so that the screen doesn't show the error state since we already have something to show which might be
   * stale, but is still more relevant than an error state.
   */
  operator fun invoke(claimId: String): Flow<Either<GetClaimDetailUseCase.Error, ClaimDetailUiState>> {
    return flow {
      val firstResult = getClaimDetailUseCase.invoke(claimId)
      emit(firstResult)
      if (firstResult.isLeft()) return@flow
      while (currentCoroutineContext().isActive) {
        getClaimDetailUseCase
          .invoke(claimId)
          .onRight { claimDetail -> emit(claimDetail.right()) }
        delay(POLL_INTERVAL)
      }
    }.map { result: Either<GetClaimDetailUseCase.Error, ClaimDetailsQuery.ClaimDetail> ->
      result.map(ClaimDetailUiState::fromDto)
    }
  }

  companion object {
    private val POLL_INTERVAL = 5.seconds
  }
}

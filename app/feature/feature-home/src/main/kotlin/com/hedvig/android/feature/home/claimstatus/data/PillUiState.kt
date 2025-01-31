package com.hedvig.android.feature.home.claimstatus.data

import giraffe.ClaimDetailsQuery
import giraffe.HomeQuery
import giraffe.type.ClaimStatusCardPillType

internal data class PillUiState(
  val text: String,
  val type: PillType,
) {

  enum class PillType {
    OPEN,
    CLOSED,
    REOPENED,
    PAYMENT,
    UNKNOWN, // Default type to not break clients on breaking API changes. Should default to how OPEN is rendered
    ;

    companion object {
      fun fromQueryType(queryType: ClaimStatusCardPillType): PillType = when (queryType) {
        ClaimStatusCardPillType.OPEN -> OPEN
        ClaimStatusCardPillType.CLOSED -> CLOSED
        ClaimStatusCardPillType.REOPENED -> REOPENED
        ClaimStatusCardPillType.PAYMENT -> PAYMENT
        ClaimStatusCardPillType.UNKNOWN__ -> UNKNOWN
      }
    }
  }

  companion object {
    fun fromClaimStatusCardsQuery(
      claimStatusCards: HomeQuery.ClaimStatusCard,
    ): List<PillUiState> = claimStatusCards.pills.map { pill ->
      PillUiState(
        text = pill.text,
        type = PillType.fromQueryType(pill.type),
      )
    }

    fun fromClaimDetailsQuery(
      claimDetail: ClaimDetailsQuery.ClaimDetail,
    ): List<PillUiState> = claimDetail.pills.map { pill ->
      PillUiState(
        text = pill.text,
        type = PillType.fromQueryType(pill.type),
      )
    }
  }
}

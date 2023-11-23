package com.hedvig.android.feature.editcoinsured.data

import java.time.format.DateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

data class CoInsured(
  val firstName: String?,
  val lastName: String?,
  val birthDate: LocalDate?,
  val ssn: String?,
  val hasMissingInfo: Boolean,
) {
  val displayName: String = buildString {
    if (firstName != null) {
      append(firstName)
    }
    if (firstName != null && lastName != null) {
      append(" ")
    }
    if (lastName != null) {
      append(lastName)
    }
  }

  fun identifier(dateTimeFormatter: DateTimeFormatter): String? =
    ssn ?: birthDate?.toJavaLocalDate()?.format(dateTimeFormatter)
}

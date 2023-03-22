package com.hedvig.android.odyssey.step.dateofoccurrence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.ui.DatePickerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class DateOfOccurrenceViewModel(
  initialDateOfOccurrence: LocalDate?,
  maxDate: LocalDate,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<DateOfOccurrenceUiState> = MutableStateFlow(
    DateOfOccurrenceUiState(
      datePickerUiState = DatePickerUiState(initiallySelectedDate = initialDateOfOccurrence, maxDate = maxDate),
      dateSubmissionError = false,
      nextStep = null,
      isLoading = false,
    ),
  )
  val uiState: StateFlow<DateOfOccurrenceUiState> = _uiState.asStateFlow()

  fun submitSelectedDate() {
    val uiState = _uiState.value
    if (!uiState.canSubmitSelectedDate()) return
    val selectedDateMillis = uiState.datePickerUiState.datePickerState.selectedDateMillis ?: return
    _uiState.update { it.copy(isLoading = true) }
    val selectedDate = Instant.fromEpochMilliseconds(selectedDateMillis).toLocalDateTime(TimeZone.UTC).date
    viewModelScope.launch {
      claimFlowRepository.submitDateOfOccurrence(selectedDate).fold(
        ifLeft = {
          _uiState.update {
            it.copy(
              dateSubmissionError = true,
              isLoading = false,
            )
          }
        },
        ifRight = { claimFlowStep: ClaimFlowStep ->
          _uiState.update {
            it.copy(
              nextStep = claimFlowStep,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update {
      it.copy(nextStep = null)
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(dateSubmissionError = false)
    }
  }
}

internal data class DateOfOccurrenceUiState(
  val datePickerUiState: DatePickerUiState,
  val dateSubmissionError: Boolean,
  val nextStep: ClaimFlowStep?,
  val isLoading: Boolean,
) {
  val canSubmit: Boolean
    @Composable
    get() = remember(
      datePickerUiState.datePickerState.selectedDateMillis,
      dateSubmissionError,
      nextStep,
      isLoading,
    ) { canSubmitSelectedDate() }
}

private fun DateOfOccurrenceUiState.canSubmitSelectedDate(): Boolean {
  return datePickerUiState.datePickerState.selectedDateMillis != null &&
    !dateSubmissionError &&
    nextStep == null &&
    !isLoading
}

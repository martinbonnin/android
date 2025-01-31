package com.hedvig.app.feature.offer.ui.changestartdate

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.flowWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogChangeStartDateBinding
import com.hedvig.app.util.extensions.epochMillisToLocalDate
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChangeDateBottomSheet : BottomSheetDialogFragment() {
  private val binding by viewBinding(DialogChangeStartDateBinding::bind)
  private val changeDateBottomSheetViewModel: ChangeDateBottomSheetViewModel by viewModel {
    val data = requireArguments().parcelable<ChangeDateBottomSheetData>(DATA)
      ?: error("No data provided to ChangeDateBottomSheet")
    parametersOf(data)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.dialog_change_start_date, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    changeDateBottomSheetViewModel
      .viewState
      .flowWithLifecycle(viewLifecycle)
      .onEach { viewState ->
        dialog?.let { dialog ->
          val isLoading = viewState is ChangeDateBottomSheetViewModel.ViewState.Loading
          val isCancellable = !isLoading
          dialog.setCancelable(isCancellable)
        }
        when (viewState) {
          ChangeDateBottomSheetViewModel.ViewState.Dismiss -> dismiss()
          is ChangeDateBottomSheetViewModel.ViewState.Inceptions -> viewState.inceptions.forEach {
            val changeDateView = createChangeDateView(it)
            binding.changeDateContainer.addView(changeDateView)
          }
          is ChangeDateBottomSheetViewModel.ViewState.Error -> {
            showLoadingState(false)
            requireContext().showAlert(
              title = getString(com.adyen.checkout.dropin.R.string.error_dialog_title),
              message = viewState.message ?: getString(com.adyen.checkout.dropin.R.string.component_error),
              positiveLabel = hedvig.resources.R.string.insurances_tab_error_button_text,
              positiveAction = { changeDateBottomSheetViewModel.setNewDateAndDismiss() },
            )
          }
          is ChangeDateBottomSheetViewModel.ViewState.Loading -> {
            showLoadingState(viewState.showLoading)
          }
        }
      }
      .launchIn(viewLifecycleScope)

    binding.chooseDateButton.setOnClickListener {
      changeDateBottomSheetViewModel.setNewDateAndDismiss()
    }
  }

  private fun createChangeDateView(inception: ChangeDateBottomSheetData.Inception): View {
    val changeDateView = ChangeDateView(requireContext())
    changeDateView.bind(
      title = if (!inception.isConcurrent) {
        inception.title
      } else {
        null
      },
      currentInsurerDisplayName = inception.currentInsurer?.displayName,
      startDate = inception.startDate,
      switchable = inception.currentInsurer?.switchable ?: false,
      datePickerListener = datePickerListener@{
        if (changeDateBottomSheetViewModel.shouldOpenDatePicker().not()) return@datePickerListener
        MaterialDatePicker.Builder
          .datePicker()
          .setTitleText("")
          .setCalendarConstraints(
            CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build(),
          )
          .build()
          .apply {
            addOnPositiveButtonClickListener { epochMillis ->
              changeDateView.setDateText(epochMillis.epochMillisToLocalDate())
              changeDateBottomSheetViewModel.onDateSelected(
                isConcurrent = inception.isConcurrent,
                quoteId = inception.quoteId,
                epochMillis = epochMillis,
              )
            }
          }
          .show(childFragmentManager, DATE_PICKER_TAG)
      },
      switchListener = { checked ->
        changeDateBottomSheetViewModel.onSwitchChecked(inception.quoteId, checked)
      },
    )
    return changeDateView
  }

  private fun showLoadingState(isLoading: Boolean) {
    TransitionManager.beginDelayedTransition(binding.root)
    binding.chooseDateButton.isEnabled = !isLoading
  }

  companion object {
    private const val DATA = "DATA"
    private const val DATE_PICKER_TAG = "DATE_PICKER_TAG"
    const val TAG = "changeDateBottomSheet"

    fun newInstance(data: ChangeDateBottomSheetData): ChangeDateBottomSheet {
      return ChangeDateBottomSheet()
        .apply {
          arguments = bundleOf(DATA to data)
        }
    }
  }
}

package com.hedvig.app.feature.embark.passages.datepicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkDatePickerBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.masking.SHORT_DATE
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter

class DatePickerFragment : Fragment(R.layout.fragment_embark_date_picker) {
  private val viewModel: EmbarkViewModel by activityViewModel()
  private val datePickerViewModel: DatePickerViewModel by viewModel()
  private val binding by viewBinding(FragmentEmbarkDatePickerBinding::bind)
  private val data: DatePickerParams
    get() = requireArguments().parcelable(DATA)
      ?: error("Programmer error: No PARAMS provided to ${this.javaClass.name}")

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.apply {
      messages.adapter = MessageAdapter(data.messages)

      dateContainer.setOnClickListener {
        datePickerViewModel.onShowDatePicker()
      }

      continueButton.applyNavigationBarInsetsMargin()

      continueButton
        .hapticClicks()
        .mapLatest { saveAndAnimate() }
        .onEach { viewModel.submitAction(data.link) }
        .launchIn(viewLifecycleScope)
    }

    datePickerViewModel.selectedDate.observe(viewLifecycleOwner) { selectedDate ->
      binding.continueButton.isEnabled = selectedDate != null
      binding.dateLabel.text = if (selectedDate != null) {
        selectedDate.format(SHORT_DATE)
      } else {
        data.placeholder
      }
      val textColor = if (selectedDate != null) {
        requireContext().getColor(R.color.textColorSecondary)
      } else {
        requireContext().getColor(R.color.textColorPrimary)
      }
      binding.dateLabel.setTextColor(textColor)
    }

    datePickerViewModel.showDatePicker.observe(viewLifecycleOwner) { selectedDate ->
      MaterialDatePicker.Builder
        .datePicker()
        .setTitleText("")
        .setCalendarConstraints(
          CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build(),
        )
        .apply { if (selectedDate != null) setSelection(selectedDate) }
        .build()
        .apply { addOnPositiveButtonClickListener { datePickerViewModel.onDateSelected(it) } }
        .show(childFragmentManager, DATE_PICKER_TAG)
    }
  }

  private suspend fun saveAndAnimate() {
    val date = datePickerViewModel.selectedDate.value?.format(DateTimeFormatter.ISO_DATE)
      ?: error("No date selected when trying to continue")
    val inputText = binding.dateLabel.text.toString()
    viewModel.putInStore("${data.passageName}Result", inputText)
    viewModel.putInStore(data.storeKey, date)
    val response = viewModel.preProcessResponse(data.passageName) ?: Response.SingleResponse(inputText)
    animateResponse(binding.responseContainer, response)
  }

  companion object {
    private const val DATA = "DATA"
    private const val DATE_PICKER_TAG = "datePicker"

    fun newInstance(data: DatePickerParams) =
      DatePickerFragment().apply {
        arguments = Bundle().apply {
          putParcelable(DATA, data)
        }
      }
  }
}

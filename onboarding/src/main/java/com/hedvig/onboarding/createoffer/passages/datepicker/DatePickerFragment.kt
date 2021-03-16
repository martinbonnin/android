package com.hedvig.onboarding.createoffer.passages.datepicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.onboarding.R
import com.hedvig.onboarding.createoffer.EmbarkViewModel
import com.hedvig.onboarding.createoffer.masking.SHORT_DATE
import com.hedvig.onboarding.createoffer.passages.MessageAdapter
import com.hedvig.onboarding.createoffer.passages.animateResponse
import com.hedvig.onboarding.databinding.FragmentEmbarkDatePickerBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class DatePickerFragment : Fragment(R.layout.fragment_embark_date_picker) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val datePickerViewModel: DatePickerViewModel by viewModel()
    private val binding by viewBinding(FragmentEmbarkDatePickerBinding::bind)
    private val data: DatePickerParams
        get() = requireArguments().getParcelable(DATA)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            messages.adapter = MessageAdapter(data.messages)

            dateContainer.setOnClickListener {
                datePickerViewModel.onShowDatePicker()
            }

            continueButton
                .hapticClicks()
                .mapLatest { saveAndAnimate() }
                .onEach { model.navigateToPassage(data.link) }
                .launchIn(viewLifecycleScope)
        }

        datePickerViewModel.selectedDate.observe(viewLifecycleOwner) { selectedDate ->
            binding.continueButton.isEnabled = selectedDate != null
            binding.dateLabel.text = if (selectedDate != null) {
                selectedDate.format(SHORT_DATE)
            } else {
                data.placeholder
            }
        }

        datePickerViewModel.showDatePicker.observe(viewLifecycleOwner) { selectedDate ->
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("")
                .apply { if (selectedDate != null) setSelection(selectedDate) }
                .build()
                .apply { addOnPositiveButtonClickListener { datePickerViewModel.onDateSelected(it) } }
                .show(childFragmentManager, DATE_PICKER_TAG)
        }
    }

    private suspend fun saveAndAnimate() {
        val inputText = binding.dateLabel.text.toString()
        model.putInStore("${data.passageName}Result", inputText)
        model.putInStore(data.storeKey, inputText)
        val responseText = model.preProcessResponse(data.passageName) ?: inputText
        animateResponse(binding.response, responseText)
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

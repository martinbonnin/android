package com.hedvig.app.feature.embark.passages.numberactionset

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.withStateAtLeast
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.android.core.common.android.whenApiVersion
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkInputItemBinding
import com.hedvig.app.databinding.NumberActionSetFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.KEYBOARD_HIDE_DELAY_DURATION
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_DURATION
import com.hedvig.app.util.extensions.addViews
import com.hedvig.app.util.extensions.hideKeyboardWithDelay
import com.hedvig.app.util.extensions.onImeAction
import com.hedvig.app.util.extensions.showKeyboardWithDelay
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

/**
 * Used for Embark actions NumberAction and NumberActionSet
 */
class NumberActionFragment : Fragment(R.layout.number_action_set_fragment) {
  private val viewModel: EmbarkViewModel by activityViewModel()
  private val binding by viewBinding(NumberActionSetFragmentBinding::bind)
  private val data: NumberActionParams
    get() = requireArguments().parcelable(PARAMS)
      ?: error("Programmer error: No PARAMS provided to ${this.javaClass.name}")
  private val numberActionViewModel: NumberActionViewModel by viewModel { parametersOf(data) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    postponeEnterTransition()

    with(binding) {
      whenApiVersion(Build.VERSION_CODES.R) {
        inputContainer.setupInsetsForIme(
          root = root,
          submit,
          inputLayout,
        )
      }

      messages.adapter = MessageAdapter(data.messages)
      val views = createInputViews()
      views.firstOrNull()?.let {
        viewLifecycleScope.launch {
          viewLifecycle.withStateAtLeast(Lifecycle.State.RESUMED) {}
          val input: TextInputEditText? = it.findViewById(R.id.input)
          context?.showKeyboardWithDelay(input, 500.milliseconds)
        }
      }
      inputContainer.addViews(views)

      numberActionViewModel.valid.observe(viewLifecycleOwner) { submit.isEnabled = it }
      submit.text = data.submitLabel
      submit
        .hapticClicks()
        .mapLatest { saveAndAnimate() }
        .onEach { viewModel.submitAction(data.link) }
        .launchIn(viewLifecycleScope)

      messages.doOnNextLayout {
        startPostponedEnterTransition()
      }

      numberActionViewModel.valid.observe(viewLifecycleOwner) {
        submit.isEnabled = it
      }
    }
  }

  private fun createInputViews(): List<View> {
    return data.numberActions.mapIndexed { index, numberAction ->
      val binding = EmbarkInputItemBinding.inflate(layoutInflater, binding.inputContainer, false)

      binding.textField.isExpandedHintEnabled = false
      numberAction.title?.let { binding.textField.hint = it }
      numberAction.unit?.let { binding.textField.helperText = it }

      binding.textField.placeholderText = numberAction.placeholder
      binding.input.doOnTextChanged { text, _, _, _ ->
        numberActionViewModel.setInputValue(numberAction.key, text.toString())
      }

      binding.input.inputType = InputType.TYPE_CLASS_NUMBER
      val imeOptions = if (index < data.numberActions.size - 1) {
        EditorInfo.IME_ACTION_NEXT
      } else {
        EditorInfo.IME_ACTION_DONE
      }
      binding.input.imeOptions = imeOptions
      if (imeOptions == EditorInfo.IME_ACTION_DONE) {
        binding.input.onImeAction(imeActionId = imeOptions) {
          if (numberActionViewModel.valid.value == true) {
            viewLifecycleScope.launch {
              saveAndAnimate()
              viewModel.submitAction(data.link)
            }
          }
        }
      }

      viewModel.getPrefillFromStore(numberAction.key)?.let { binding.input.setText(it) }
      binding.root
    }
  }

  private suspend fun saveAndAnimate() {
    context?.hideKeyboardWithDelay(
      inputView = binding.inputLayout,
      delayDuration = KEYBOARD_HIDE_DELAY_DURATION,
    )
    numberActionViewModel.onContinue(viewModel::putInStore)
    val allInput = numberActionViewModel.getAllInput()
    val response =
      viewModel.preProcessResponse(data.passageName)
        ?: Response.SingleResponse(allInput ?: "")
    animateResponse(binding.responseContainer, response)
    delay(PASSAGE_ANIMATION_DELAY_DURATION)
  }

  companion object {
    private const val PARAMS = "PARAMS"
    fun newInstance(params: NumberActionParams) = NumberActionFragment().apply {
      arguments = bundleOf(
        PARAMS to params,
      )
    }
  }
}

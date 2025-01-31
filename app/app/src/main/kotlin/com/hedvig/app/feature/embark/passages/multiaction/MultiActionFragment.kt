package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkMultiActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet.Companion.ADD_COMPONENT_REQUEST_KEY
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_DURATION
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.parameter.parametersOf

class MultiActionFragment : Fragment(R.layout.fragment_embark_multi_action) {
  private val viewModel: EmbarkViewModel by activityViewModel()

  private val multiActionParams: MultiActionParams by lazy {
    requireArguments().parcelable(DATA)
      ?: error("Programmer error: No PARAMS provided to ${this.javaClass.name}")
  }

  private val multiActionViewModel: MultiActionViewModel by activityViewModel { parametersOf(multiActionParams) }
  private val binding by viewBinding(FragmentEmbarkMultiActionBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    postponeEnterTransition()

    binding.continueButton.applyNavigationBarInsetsMargin()

    setFragmentResultListener(ADD_COMPONENT_REQUEST_KEY) { requestKey: String, bundle: Bundle ->
      if (requestKey == ADD_COMPONENT_REQUEST_KEY) {
        bundle.parcelable<MultiActionItem.Component>(AddComponentBottomSheet.RESULT)?.let {
          multiActionViewModel.onComponentCreated(it)
        }
      }
    }

    val adapter = MultiActionAdapter(
      multiActionViewModel::onComponentClicked,
      multiActionViewModel::onComponentRemoved,
      multiActionViewModel::createNewComponent,
    )
    binding.apply {
      messages.adapter = MessageAdapter(multiActionParams.messages)
      componentContainer.adapter = adapter
      continueButton.text = multiActionParams.submitLabel

      messages.doOnNextLayout {
        startPostponedEnterTransition()
      }
    }

    multiActionViewModel
      .components
      .flowWithLifecycle(viewLifecycle)
      .onEach(adapter::submitList)
      .launchIn(viewLifecycleScope)

    multiActionViewModel
      .newComponent
      .flowWithLifecycle(viewLifecycle)
      .onEach(::showAddBuildingSheet)
      .launchIn(viewLifecycleScope)

    binding.continueButton
      .hapticClicks()
      .mapLatest { saveAndAnimate() }
      .onEach {
        viewModel.submitAction(multiActionParams.link)
      }
      .launchIn(viewLifecycleScope)
  }

  private suspend fun saveAndAnimate() {
    multiActionViewModel.onContinue(viewModel::putInStore)
    val response =
      viewModel.preProcessResponse(multiActionParams.passageName) ?: Response.SingleResponse("")
    animateResponse(binding.responseContainer, response)
    delay(PASSAGE_ANIMATION_DELAY_DURATION)
  }

  private fun showAddBuildingSheet(componentState: MultiActionItem.Component?) {
    AddComponentBottomSheet
      .newInstance(componentState, multiActionParams)
      .show(parentFragmentManager, BOTTOM_SHEET_TAG)
  }

  companion object {
    private const val DATA = "DATA"
    private const val BOTTOM_SHEET_TAG = "BOTTOM_SHEET_TAG"

    fun newInstance(data: MultiActionParams) =
      MultiActionFragment().apply {
        arguments = Bundle().apply {
          putParcelable(DATA, data)
        }
      }
  }
}

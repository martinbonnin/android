package com.hedvig.app.feature.embark.passages.previousinsurer

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import coil.ImageLoader
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.common.android.parcelableArrayList
import com.hedvig.app.ui.view.ExpandableBottomSheet
import org.koin.android.ext.android.inject

class InsurerProviderBottomSheet : ExpandableBottomSheet() {

  private val imageLoader: ImageLoader by inject()
  private val hedvigBuildConstants: HedvigBuildConstants by inject()

  private val insurers by lazy {
    requireArguments().parcelableArrayList<PreviousInsurerParameter.PreviousInsurer>(PREVIOUS_INSURERS)
      ?: error("No argument passed to ${this.javaClass.name}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recycler.adapter = PreviousInsurerAdapter(
      context = requireContext(),
      previousInsurers = insurers,
      hedvigBuildConstants = hedvigBuildConstants,
      imageLoader = imageLoader,
      onInsurerClicked = ::onInsurerSelected,
    )
  }

  private fun onInsurerSelected(item: PreviousInsurerItem.Insurer) {
    setFragmentResult(
      REQUEST_KEY,
      bundleOf(
        Pair(INSURER_ID_KEY, item.id),
        Pair(INSURER_COLLECTION_ID_KEY, item.collectionId),
        Pair(INSURER_NAME_KEY, item.name),
      ),
    )
    dismiss()
  }

  companion object {

    val TAG: String = InsurerProviderBottomSheet::class.java.name
    const val REQUEST_KEY = "INSURER_BOTTOM_SHEET_KEY"
    const val INSURER_ID_KEY = "INSURER_ID_KEY"
    const val INSURER_COLLECTION_ID_KEY = "INSURER_COLLECTION_ID_KEY"
    const val INSURER_NAME_KEY = "INSURER_NAME_KEY"
    private const val PREVIOUS_INSURERS = "PREVIOUS_INSURERS"

    fun newInstance(previousInsurers: List<PreviousInsurerParameter.PreviousInsurer>) =
      InsurerProviderBottomSheet().apply {
        arguments = bundleOf(PREVIOUS_INSURERS to previousInsurers)
      }
  }
}

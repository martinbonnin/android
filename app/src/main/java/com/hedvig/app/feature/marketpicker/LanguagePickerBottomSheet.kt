package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.LanguagePickerBottomSheetBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class LanguagePickerBottomSheet : BottomSheetDialogFragment() {
    val binding by viewBinding(LanguagePickerBottomSheetBinding::bind)

    private val model: MarketPickerViewModel by sharedViewModel()
    private val tracker: MarketPickerTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.language_picker_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            recycler.adapter = LanguagePickerBottomSheetAdapter(model, tracker, dialog)
            model.data.observe(viewLifecycleOwner) { state ->
                state.market?.let { market ->
                    (recycler.adapter as? LanguagePickerBottomSheetAdapter)?.items =
                        listOf(
                            LanguageAdapterModel.Header,
                            LanguageAdapterModel.Description,
                            LanguageAdapterModel.LanguageList(Language.getAvailableLanguages(market))
                        )
                }
            }
        }
    }

    companion object {
        const val TAG = "LanguagePickerBottomSheet"
    }
}

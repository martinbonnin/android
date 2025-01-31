package com.hedvig.app.feature.embark.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.core.common.android.isDarkThemeActive
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.android.core.common.android.remove
import com.hedvig.android.core.common.android.show
import com.hedvig.android.core.ui.view.viewDps
import com.hedvig.app.R
import com.hedvig.app.databinding.TooltipBottomSheetBinding
import com.hedvig.app.feature.embark.TooltipModel
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.windowHeight
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import giraffe.EmbarkStoryQuery
import org.koin.androidx.viewmodel.ext.android.getViewModel

class TooltipBottomSheet : BottomSheetDialogFragment() {
  private val binding by viewBinding(TooltipBottomSheetBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View = inflater.inflate(R.layout.tooltip_bottom_sheet, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    getViewModel<TooltipViewModel>()
    val tooltipsParcel = requireArguments().parcelable<TooltipsParcel>(TOOLTIPS)
      ?: error("Programmer error: no tooltips passed to ${this::class.java.name}")
    val tooltips = tooltipsParcel.tooltips
    binding.apply {
      recycler.adapter = TooltipBottomSheetAdapter().also { adapter ->
        adapter.submitList(
          if (tooltips.size == 1) {
            listOf(
              TooltipModel.Header(tooltips.first().title),
              *getTooltipsWithoutTitles(tooltips),
            )
          } else {
            listOf(
              TooltipModel.Header(),
              *getTooltipsWithTitles(tooltips),
            )
          },
        )
      }

      if (tooltips.size > 1) {
        val defaultStatusBarColor = dialog?.window?.statusBarColor
        close.alpha = 0f
        (dialog as? BottomSheetDialog)?.behavior?.let { behaviour ->
          val windowHeight = requireActivity().windowHeight

          recycler.measure(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
          )
          val sheetContentHeight =
            recycler.measuredHeight + recycler.marginTop + recycler.marginBottom + resources.getDimension(
              R.dimen.peril_bottom_sheet_close_icon_size,
            )
              .toInt().viewDps
          val shouldPeekAtContentHeight = sheetContentHeight < windowHeight
          val defaultPeekHeight = 295.viewDps
          if (shouldPeekAtContentHeight) {
            behaviour.setPeekHeight(windowHeight, true)
            chevronContainer.remove()
          } else {
            behaviour.setPeekHeight(defaultPeekHeight, true)
            chevronContainer.show()
          }
          if (!shouldPeekAtContentHeight) {
            chevronContainer.measure(
              FrameLayout.LayoutParams.MATCH_PARENT,
              FrameLayout.LayoutParams.WRAP_CONTENT,
            )
            val chevronContainerHeight = chevronContainer.measuredHeight
            val startTranslation = (defaultPeekHeight - chevronContainerHeight).toFloat()
            chevronContainer.translationY = startTranslation
            behaviour.addBottomSheetCallback(
              object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                  when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                      dialog?.window?.statusBarColor =
                        requireContext().colorAttr(com.google.android.material.R.attr.colorSurface)
                      if (!requireContext().isDarkThemeActive) {
                        dialog?.window?.decorView?.let {
                          val insetsController =
                            WindowInsetsControllerCompat(requireActivity().window, view)
                          insetsController.isAppearanceLightStatusBars = true
                        }
                      }
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                      defaultStatusBarColor?.let {
                        dialog?.window?.statusBarColor = it
                      }
                      close.show()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                      close.remove()
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    BottomSheetBehavior.STATE_HIDDEN,
                    BottomSheetBehavior.STATE_SETTLING,
                    -> {
                      // No-op
                    }
                  }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                  close.alpha = slideOffset
                  chevronContainer.translationY =
                    boundedLerp(
                      startTranslation,
                      (binding.root.height - chevronContainer.height).toFloat(),
                      slideOffset,
                    )
                  binding.root.height
                  chevronContainer.alpha = 1 - slideOffset
                }
              },
            )
          }
          chevron.setHapticClickListener {
            close.show()
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
          }
        }
      }
      close.setHapticClickListener {
        this@TooltipBottomSheet.dismiss()
      }
    }
  }

  companion object {
    private const val TOOLTIPS = "TOOLTIPS"

    val TAG: String = TooltipBottomSheet::class.java.name
    fun newInstance(tooltips: List<EmbarkStoryQuery.Tooltip>) =
      TooltipBottomSheet().apply {
        val parcelableTooltips: List<Tooltip> = buildList {
          tooltips.forEach {
            add(
              Tooltip(
                title = it.title,
                description = it.description,
              ),
            )
          }
        }
        arguments = bundleOf(TOOLTIPS to TooltipsParcel(parcelableTooltips))
      }

    fun getTooltipsWithTitles(list: List<Tooltip>) =
      list.map { TooltipModel.Tooltip.TooltipWithTitle(it.title, it.description) }.toTypedArray()

    fun getTooltipsWithoutTitles(list: List<Tooltip>) =
      list.map { TooltipModel.Tooltip.TooltipWithOutTitle(it.description) }.toTypedArray()
  }
}

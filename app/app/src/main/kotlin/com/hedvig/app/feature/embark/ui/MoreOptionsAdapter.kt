package com.hedvig.app.feature.embark.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.core.common.android.GenericDiffUtilItemCallback
import com.hedvig.android.core.ui.view.viewDps
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.MoreOptionsRowBinding
import com.hedvig.app.isDebug
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.putCompoundDrawablesRelativeWithIntrinsicBounds
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class MoreOptionsAdapter(private val viewModel: MemberIdViewModel) :
  ListAdapter<MoreOptionsModel, MoreOptionsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    HEADER -> ViewHolder.Header(parent)
    MEMBER_ID_SUCCESS -> ViewHolder.UserIdSuccess(parent)
    MEMBER_ID_ERROR -> ViewHolder.UserIdError(parent)
    VERSION -> ViewHolder.Version(parent)
    COPYRIGHT -> ViewHolder.Copyright(parent)
    else -> throw Error("Unreachable")
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position), viewModel)
  }

  override fun getItemViewType(position: Int) = when (getItem(position)) {
    MoreOptionsModel.Header -> HEADER
    is MoreOptionsModel.UserId.Success -> MEMBER_ID_SUCCESS
    MoreOptionsModel.UserId.Error -> MEMBER_ID_ERROR
    MoreOptionsModel.Version -> VERSION
    MoreOptionsModel.Copyright -> COPYRIGHT
  }

  sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: MoreOptionsModel, viewModel: MemberIdViewModel)

    class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.more_options_header)) {
      override fun bind(item: MoreOptionsModel, viewModel: MemberIdViewModel) = Unit
    }

    class UserIdSuccess(parent: ViewGroup) :
      ViewHolder(parent.inflate(R.layout.more_options_row)) {
      private val binding by viewBinding(MoreOptionsRowBinding::bind)
      override fun bind(item: MoreOptionsModel, viewModel: MemberIdViewModel) {
        if (item !is MoreOptionsModel.UserId.Success) {
          invalid(item)
          return
        }
        binding.apply {
          label.apply {
            setText(hedvig.resources.R.string.embark_onboarding_more_options_user_id_label)
            compoundDrawablePadding = 16.viewDps
            putCompoundDrawablesRelativeWithIntrinsicBounds(start = R.drawable.ic_contact_information)
          }
          info.text = item.id
        }
      }
    }

    class UserIdError(parent: ViewGroup) :
      ViewHolder(parent.inflate(R.layout.more_options_row)) {
      private val binding by viewBinding(MoreOptionsRowBinding::bind)
      override fun bind(item: MoreOptionsModel, viewModel: MemberIdViewModel) {
        if (item !is MoreOptionsModel.UserId.Error) {
          invalid(item)
          return
        }
        binding.apply {
          label.apply {
            setText(hedvig.resources.R.string.embark_onboarding_more_options_user_id_label)
            compoundDrawablePadding = 16.viewDps
            putCompoundDrawablesRelativeWithIntrinsicBounds(start = R.drawable.ic_contact_information)
          }
          info.apply {
            setText(hedvig.resources.R.string.embark_onboarding_more_options_loading_error_reload_label)
            compoundDrawablePadding = 8.viewDps
            putCompoundDrawablesRelativeWithIntrinsicBounds(end = R.drawable.ic_refresh)
            setHapticClickListener {
              viewModel.load()
            }
          }
        }
      }
    }

    class Version(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.more_options_row)) {
      private val binding by viewBinding(MoreOptionsRowBinding::bind)
      override fun bind(item: MoreOptionsModel, viewModel: MemberIdViewModel) {
        binding.apply {
          label.apply {
            setText(hedvig.resources.R.string.embark_onboarding_more_options_version_label)
            compoundDrawablePadding = 16.viewDps
            putCompoundDrawablesRelativeWithIntrinsicBounds(start = R.drawable.ic_info_more_options)
          }
          info.text = buildString {
            append(BuildConfig.VERSION_NAME)
            if (isDebug()) {
              append(" (")
              append(BuildConfig.VERSION_CODE)
              append(")")
            }
          }
        }
      }
    }

    class Copyright(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.copyright)) {
      override fun bind(item: MoreOptionsModel, viewModel: MemberIdViewModel) = Unit
    }
  }

  companion object {
    private const val HEADER = 1
    private const val MEMBER_ID_SUCCESS = 2
    private const val MEMBER_ID_ERROR = 3
    private const val VERSION = 4
    private const val COPYRIGHT = 5
  }
}

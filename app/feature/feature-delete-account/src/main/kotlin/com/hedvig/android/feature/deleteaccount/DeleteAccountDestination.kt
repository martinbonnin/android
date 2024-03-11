package com.hedvig.android.feature.deleteaccount

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import hedvig.resources.R

@Composable
internal fun DeleteAccountDestination(
  viewModel: DeleteAccountViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  DeleteAccountScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
    retryLoading = { viewModel.emit(DeleteAccountEvent.RetryLoading) },
    initiateAccountDeletion = { viewModel.emit(DeleteAccountEvent.InitiateAccountDeletion) },
  )
}

@Composable
private fun DeleteAccountScreen(
  uiState: DeleteAccountUiState,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  retryLoading: () -> Unit,
  initiateAccountDeletion: () -> Unit,
) {
  HedvigScaffold(navigateUp = navigateUp) {
    when (uiState) {
      DeleteAccountUiState.FailedToLoadDeleteAccountState -> {
        HedvigErrorSection(onButtonClick = retryLoading, Modifier.weight(1f))
      }

      DeleteAccountUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f))
      }

      is DeleteAccountUiState.CanNotDelete -> {
        DeleteScreenContents(
          title = stringResource(uiState.titleStringRes()),
          description = stringResource(uiState.descriptionStringRes()),
          buttonText = stringResource(R.string.general_back_button),
          onButtonClick = navigateBack,
          modifier = Modifier.weight(1f),
        )
      }

      is DeleteAccountUiState.CanDelete -> {
        if (uiState.failedToPerformDeletion) {
          HedvigErrorSection(onButtonClick = retryLoading, Modifier.weight(1f))
        } else {
          DeleteScreenContents(
            title = stringResource(R.string.DELETE_ACCOUNT_DELETE_ACCOUNT_TITLE),
            description = stringResource(R.string.DELETE_ACCOUNT_DELETE_ACCOUNT_DESCRIPTION),
            buttonText = stringResource(R.string.PROFILE_DELETE_ACCOUNT_CONFIRM_DELETION),
            onButtonClick = initiateAccountDeletion,
            modifier = Modifier.weight(1f),
            isButtonLoading = uiState.isPerformingDeletion,
            buttonColors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
          )
        }
      }
    }
  }
}

@Composable
private fun DeleteScreenContents(
  title: String,
  description: String,
  buttonText: String,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
  isButtonLoading: Boolean = false,
  buttonColors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
  ),
) {
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(32.dp))
    RichText(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      Markdown(
        content = description,
      )
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = buttonText,
      onClick = onButtonClick,
      isLoading = isButtonLoading,
      colors = buttonColors,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@StringRes
private fun DeleteAccountUiState.CanNotDelete.titleStringRes(): Int {
  return when (this) {
    DeleteAccountUiState.CanNotDelete.AlreadyRequestedDeletion -> R.string.DELETE_ACCOUNT_PROCESSED_TITLE
    DeleteAccountUiState.CanNotDelete.HasActiveInsurance -> R.string.DELETE_ACCOUNT_YOU_HAVE_ACTIVE_INSURANCE_TITLE
    DeleteAccountUiState.CanNotDelete.HasOngoingClaim -> R.string.DELETE_ACCOUNT_YOU_HAVE_ACTIVE_CLAIM_TITLE
  }
}

@StringRes
private fun DeleteAccountUiState.CanNotDelete.descriptionStringRes(): Int {
  return when (this) {
    DeleteAccountUiState.CanNotDelete.AlreadyRequestedDeletion ->
      R.string.DELETE_ACCOUNT_PROCESSED_DESCRIPTION
    DeleteAccountUiState.CanNotDelete.HasActiveInsurance ->
      R.string.DELETE_ACCOUNT_YOU_HAVE_ACTIVE_INSURANCE_DESCRIPTION
    DeleteAccountUiState.CanNotDelete.HasOngoingClaim ->
      R.string.DELETE_ACCOUNT_YOU_HAVE_ACTIVE_CLAIM_DESCRIPTION
  }
}

@HedvigPreview
@Composable
private fun PreviewDeleteAccountScreen(
  @PreviewParameter(DeleteAccountUiStateProvider::class) uiState: DeleteAccountUiState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DeleteAccountScreen(uiState, {}, {}, {}, {})
    }
  }
}

private class DeleteAccountUiStateProvider : CollectionPreviewParameterProvider<DeleteAccountUiState>(
  listOf(
    DeleteAccountUiState.Loading,
    DeleteAccountUiState.FailedToLoadDeleteAccountState,
    DeleteAccountUiState.CanNotDelete.AlreadyRequestedDeletion,
    DeleteAccountUiState.CanNotDelete.HasOngoingClaim,
    DeleteAccountUiState.CanNotDelete.HasActiveInsurance,
    DeleteAccountUiState.CanDelete(true, false),
    DeleteAccountUiState.CanDelete(false, true),
    DeleteAccountUiState.CanDelete(false, false),
  ),
)

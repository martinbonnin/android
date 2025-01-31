package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import hedvig.resources.R

@Composable
internal fun EditInsuranceBottomSheetContent(
  allowEditCoInsured: Boolean,
  onEditCoInsuredClick: () -> Unit,
  onChangeAddressClick: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedItemIndex by rememberSaveable { mutableStateOf(-1) }
  Column(
    modifier = modifier,
  ) {
    Text(
      text = stringResource(id = R.string.CONTRACT_CHANGE_INFORMATION_TITLE),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    )
    Spacer(modifier = Modifier.height(32.dp))
    SelectableItem(
      text = stringResource(R.string.insurance_details_change_address_button),
      isSelected = selectedItemIndex == 0,
      onClick = {
        selectedItemIndex = if (selectedItemIndex == 0) {
          -1
        } else {
          0
        }
      },
    )
    if (allowEditCoInsured) {
      Spacer(modifier = Modifier.height(4.dp))
      SelectableItem(
        text = stringResource(R.string.CONTRACT_EDIT_COINSURED),
        isSelected = selectedItemIndex == 1,
        onClick = {
          selectedItemIndex = if (selectedItemIndex == 1) {
            -1
          } else {
            1
          }
        },
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    AnimatedVisibility(visible = selectedItemIndex == 1) {
      VectorInfoCard(
        text = stringResource(id = R.string.insurances_tab_contact_us_to_edit_co_insured),
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.general_continue_button),
      onClick = {
        if (selectedItemIndex == 0) {
          onChangeAddressClick()
        } else if (selectedItemIndex == 1 && allowEditCoInsured) {
          onEditCoInsuredClick()
        }
      },
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_cancel_button),
      onClick = onDismiss,
    )
    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun SelectableItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(72.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      SelectIndicationCircle(isSelected)
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewEditInsuranceBottomSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      EditInsuranceBottomSheetContent(
        allowEditCoInsured = true,
        onEditCoInsuredClick = {},
        onChangeAddressClick = {},
        onDismiss = {},
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

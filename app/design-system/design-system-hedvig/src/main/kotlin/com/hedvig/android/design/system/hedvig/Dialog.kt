package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.BIG
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.SMALL
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.tokens.DialogTokens
import hedvig.resources.R

@Composable
fun HedvigDialogError(
  titleText: String,
  descriptionText: String,
  buttonText: String,
  onButtonClick: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigDialog(
    style = NoButtons,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
  ) {
    EmptyState(
      text = titleText,
      description = descriptionText,
      iconStyle = ERROR,
      buttonStyle = EmptyStateButtonStyle.Button(
        buttonText = buttonText,
        onButtonClick = onButtonClick,
      ),
    )
  }
}

@Composable
fun HedvigAlertDialog(
  title: String,
  text: String?,
  onConfirmClick: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  confirmButtonLabel: String = stringResource(R.string.GENERAL_YES),
  dismissButtonLabel: String = stringResource(R.string.GENERAL_NO),
) {
  HedvigDialog(
    style = Buttons(
      confirmButtonText = confirmButtonLabel,
      dismissButtonText = dismissButtonLabel,
      onDismissRequest = onDismissRequest,
      onConfirmButtonClick = onConfirmClick,
    ),
    onDismissRequest = onDismissRequest,
    modifier = modifier,
  ) {
    Column(
      Modifier
        .fillMaxWidth()
        .padding(top = 24.dp, start = 24.dp, end = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      EmptyState(
        text = title,
        description = text,
        iconStyle = EmptyStateDefaults.EmptyStateIconStyle.NO_ICON,
        buttonStyle = EmptyStateButtonStyle.NoButton,
      )
    }
  }
}

@Composable
fun SingleSelectDialog(
  title: String,
  optionsList: List<RadioOptionData>,
  onSelected: (RadioOptionData) -> Unit,
  onDismissRequest: () -> Unit,
  radioOptionStyle: RadioOptionStyle = RadioOptionStyle.LeftAligned,
  radioOptionSize: RadioOptionDefaults.RadioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
) {
  HedvigDialog(onDismissRequest = { onDismissRequest.invoke() }) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(16.dp),
    ) {
      Spacer(Modifier.height(8.dp))
      HedvigText(title, style = HedvigTheme.typography.bodySmall, textAlign = TextAlign.Center)
      Spacer(Modifier.height(24.dp))
      optionsList.forEachIndexed { index, radioOptionData ->
        RadioOption(
          data = radioOptionData,
          radioOptionStyle = radioOptionStyle,
          radioOptionSize = radioOptionSize,
          groupLockedState = NotLocked,
          onOptionClick = {
            onSelected(radioOptionData)
            onDismissRequest()
          },
        )
        if (index != optionsList.lastIndex) {
          Spacer(Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
fun HedvigDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  applyDefaultPadding: Boolean = false,
  style: DialogStyle = DialogDefaults.defaultDialogStyle,
  content: @Composable () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogDefaults.defaultProperties,
  ) {
    // (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0.2f)
    // a workaround to stop the overlay from dimming background too much,
    // otherwise in the dark theme the overlay color
    // becomes the same as the background color of the dialog itself.
    // todo: that workaround stopped working btw
    Surface(
      shape = DialogDefaults.shape,
      color = DialogDefaults.containerColor,
      modifier = modifier,
    ) {
      val padding = if (applyDefaultPadding) DialogDefaults.padding else PaddingValues()
      Column(
        Modifier.padding(padding),
      ) {
        when (style) {
          is Buttons -> {
            content()
            Spacer(Modifier.height(16.dp))
            when (style.buttonSize) {
              BIG -> {
                BigVerticalButtons(
                  onDismissRequest = style.onDismissRequest,
                  dismissButtonText = style.dismissButtonText,
                  onConfirmButtonClick = style.onConfirmButtonClick,
                  confirmButtonText = style.confirmButtonText,
                )
              }

              SMALL -> {
                SmallHorizontalButtons(
                  onDismissRequest = style.onDismissRequest,
                  dismissButtonText = style.dismissButtonText,
                  onConfirmButtonClick = style.onConfirmButtonClick,
                  confirmButtonText = style.confirmButtonText,
                )
              }
            }
          }

          NoButtons -> content()
        }
      }
    }
  }
}

@Composable
private fun SmallHorizontalButtons(
  onDismissRequest: () -> Unit,
  dismissButtonText: String,
  onConfirmButtonClick: () -> Unit,
  confirmButtonText: String,
) {
  Row(
    Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    HedvigButton(
      modifier = Modifier.weight(1f),
      onClick = onDismissRequest,
      text = dismissButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
    )
    Spacer(Modifier.width(8.dp))
    HedvigButton(
      modifier = Modifier.weight(1f),
      onClick = onConfirmButtonClick,
      text = confirmButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Primary,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
    )
  }
}

@Composable
private fun BigVerticalButtons(
  onDismissRequest: () -> Unit,
  dismissButtonText: String,
  onConfirmButtonClick: () -> Unit,
  confirmButtonText: String,
) {
  Column {
    HedvigButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = onConfirmButtonClick,
      text = confirmButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Primary,
      buttonSize = ButtonDefaults.ButtonSize.Large,
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = onDismissRequest,
      text = dismissButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Large,
    )
  }
}

object DialogDefaults {
  internal val defaultButtonSize = SMALL
  internal val defaultDialogStyle = NoButtons
  internal val defaultProperties = DialogProperties()

  internal val shape: Shape
    @Composable
    @ReadOnlyComposable
    get() = DialogTokens.ContainerShape.value

  internal val containerColor: Color
    @Composable
    get() = with(HedvigTheme.colorScheme) {
      remember(this) {
        fromToken(DialogTokens.ContainerColor)
      }
    }

  internal val padding = PaddingValues(DialogTokens.Padding)

  sealed class DialogStyle {
    data class Buttons(
      val onDismissRequest: () -> Unit,
      val dismissButtonText: String,
      val onConfirmButtonClick: () -> Unit,
      val confirmButtonText: String,
      val buttonSize: ButtonSize = defaultButtonSize,
    ) : DialogStyle()

    data object NoButtons : DialogStyle()
  }

  enum class ButtonSize {
    BIG,
    SMALL,
  }
}

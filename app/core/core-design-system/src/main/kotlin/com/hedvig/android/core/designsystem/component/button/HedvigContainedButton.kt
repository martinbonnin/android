package com.hedvig.android.core.designsystem.component.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.animation.ThreeDotsLoading
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun HedvigContainedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
  ),
) {
  HedvigContainedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    ButtonText(text)
  }
}

@Composable
fun HedvigContainedButton(
  text: String,
  onClick: () -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
  ),
) {
  HedvigContainedButton(
    onClick = {
      if (enabled && !isLoading) {
        onClick()
      }
    },
    modifier = modifier,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    val loadingTransition = updateTransition(isLoading)
    loadingTransition.AnimatedContent(
      transitionSpec = {
        fadeIn(tween(durationMillis = 220, delayMillis = 90)) with fadeOut(tween(90))
      },
      contentAlignment = Alignment.Center,
    ) { isLoading ->
      if (isLoading) {
        Box(
          contentAlignment = Alignment.Center,
        ) {
          // render the text too so that the same space is taken in all cases
          ButtonText(text, Modifier.alpha(0f))
          ThreeDotsLoading()
        }
      } else {
        ButtonText(text)
      }
    }
  }
}

@Composable
private fun HedvigContainedButton(
  onClick: () -> Unit,
  colors: ButtonColors,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.squircleMedium,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    content()
  }
}

@Composable
private fun ButtonText(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewHedvigContainedButton() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigContainedButton("Hello there", {}, Modifier.padding(24.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewLoadingHedvigContainedButton() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigContainedButton(
        text = "Hello there",
        onClick = {},
        isLoading = true,
        modifier = Modifier.padding(24.dp),
      )
    }
  }
}

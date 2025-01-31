package com.hedvig.app.feature.offer.ui.composable.variants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvig_black
import com.hedvig.android.core.designsystem.theme.hedvig_black12percent
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.app.util.compose.RadioButton

@Composable
fun VariantButton(
  id: String,
  title: String,
  tag: String?,
  description: String?,
  cost: String,
  selected: Boolean,
  onClick: (id: String) -> Unit,
) {
  Card(
    onClick = { onClick(id) },
    border = if (selected) {
      BorderStroke(2.dp, hedvig_black)
    } else {
      BorderStroke(1.dp, hedvig_black12percent)
    },
    modifier = Modifier
      .padding(top = 8.dp, start = 16.dp, end = 16.dp)
      .heightIn(min = 110.dp),
  ) {
    Row(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 16.dp)) {
      RadioButton(
        selected = selected,
        size = 24.dp,
        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
      )
      Spacer(Modifier.width(12.dp))
      Column {
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            Text(
              text = title,
              style = MaterialTheme.typography.bodyLarge,
            )
          },
          endSlot = {
            Text(
              text = cost,
              style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
              textAlign = TextAlign.End,
            )
          },
          spaceBetween = 10.dp,
        )
        if (tag != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = tag,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
          )
        }
        if (description != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewVariantButton(
  @PreviewParameter(VariantButtonInputsProvider::class) inputs: Triple<String, String, Boolean>,
) {
  val (title, subtitle, selected) = inputs
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      VariantButton(
        id = "id",
        title = title,
        tag = subtitle,
        description = "Test description",
        cost = "12923 NOK",
        selected = selected,
        onClick = {},
      )
    }
  }
}

private class VariantButtonInputsProvider : CollectionPreviewParameterProvider<Triple<String, String, Boolean>>(
  listOf(
    Triple("Hemförsäkring och Olyckssfall", "Test subtitle", true),
    Triple("Title".repeat(5), "Subtitle", false),
    Triple("Title", "Subtitle".repeat(5), false),
    Triple("Title".repeat(10), "Subtitle".repeat(10), true),
  ),
)

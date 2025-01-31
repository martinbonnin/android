package com.hedvig.app.feature.embark.passages.selectaction.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.hedvig.android.core.ui.grid.HedvigGrid
import com.hedvig.android.core.ui.grid.InsideGridSpace
import com.hedvig.app.feature.embark.passages.selectaction.SelectActionParameter
import com.hedvig.app.ui.compose.composables.CenteredContentWithTopBadge

@Composable
fun SelectActionView(
  selectActions: List<SelectActionParameter.SelectAction>,
  onActionClick: (SelectActionParameter.SelectAction, Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigGrid(
    modifier = modifier,
    contentPadding = PaddingValues(16.dp),
    insideGridSpace = InsideGridSpace(8.dp),
    centerLastItem = true,
  ) {
    selectActions.forEachIndexed { index, selectAction ->
      SelectActionCard(
        text = selectAction.label,
        badge = selectAction.badge,
        onClick = {
          onActionClick(selectAction, index)
        },
      )
    }
  }
}

@Composable
private fun SelectActionCard(
  text: String,
  badge: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    modifier = modifier.heightIn(min = 80.dp),
  ) {
    CenteredContentWithTopBadge(
      modifier = Modifier.padding(8.dp),
      centeredContent = {
        Text(
          text = text,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(vertical = 8.dp),
        )
      },
      topContent = badge?.let {
        { BadgeText(badge) }
      },
    )
  }
}

@Composable
private fun BadgeText(badge: String) {
  Text(
    text = badge,
    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    textAlign = TextAlign.Center,
  )
}

@HedvigPreview
@Composable
private fun PreviewSelectActionView(
  @PreviewParameter(SelectActionsCollection::class) selectActions: List<Pair<String, String?>>,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SelectActionView(
        selectActions = selectActions.map { (text, badge) ->
          SelectActionParameter.SelectAction(
            link = "",
            label = text,
            keys = emptyList(),
            values = emptyList(),
            badge = badge,
          )
        },
        onActionClick = { _, _ -> },
      )
    }
  }
}

private class SelectActionsCollection : CollectionPreviewParameterProvider<List<Pair<String, String?>>>(
  List(3) { listSize ->
    List(listSize + 1) { index ->
      if (index % 2 == 0) {
        "Text#$index".repeat(10 * (index + 1)) to "badge#$index"
      } else {
        "Badgeless#$index".repeat(4 * (index + 1)) to null
      }
    }
  },
)

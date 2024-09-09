package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.TopAppBarDefaults.windowInsets
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.TypographyKeyTokens

@Composable
fun Scaffold(
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  topAppBarText: String? = null,
  topAppBarActionType: TopAppBarActionType = TopAppBarActionType.BACK,
  itemsColumnHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
  topAppBarActions: @Composable RowScope.() -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary, // todo: to tokens
    modifier = modifier.fillMaxSize(),
  ) {
    Column(
      Modifier
        .fillMaxSize(),
    ) {
      TopAppBar(
        title = topAppBarText ?: "",
        onClick = navigateUp,
        actionType = topAppBarActionType,
        topAppBarActions = topAppBarActions,
      )
      Column(
        horizontalAlignment = itemsColumnHorizontalAlignment,
        modifier = Modifier
          .fillMaxSize()
          .windowInsetsPadding(
            WindowInsets.safeDrawing.only(
              WindowInsetsSides.Horizontal +
                WindowInsetsSides.Bottom,
            ),
          ),
      ) {
        content()
      }
    }
  }
}

@Composable
fun TopAppBarLayoutForActions(modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
  Row(
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .windowInsetsPadding(
        windowInsets,
      )
      .height(64.dp) // todo: to tokens
      .fillMaxWidth()
      .padding(horizontal = 16.dp), // todo: to tokens
  ) {
    actions()
  }
}

@Composable
fun TopAppBarWithBack(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
  TopAppBar(
    title = title,
    onClick = onClick,
    actionType = TopAppBarActionType.BACK,
    modifier = modifier,
    windowInsets = windowInsets,
  )
}

enum class TopAppBarActionType {
  BACK,
  CLOSE,
}

internal object TopAppBarDefaults {
  val windowInsets: WindowInsets
    @Composable
    get() = WindowInsets.systemBars
      .union(WindowInsets.displayCutout)
      .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
}

@Composable
fun TopAppBar(
  title: String,
  onClick: () -> Unit,
  actionType: TopAppBarActionType,
  modifier: Modifier = Modifier,
  topAppBarActions: @Composable (RowScope.() -> Unit)? = null,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .windowInsetsPadding(windowInsets)
        .height(64.dp) // todo: to tokens
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp), // todo: to tokens. but also we don't really know them,
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = { onClick() },
            modifier = Modifier.size(24.dp),
            content = {
              Icon(
                imageVector = when (actionType) {
                  TopAppBarActionType.BACK -> HedvigIcons.ArrowLeft // todo: check here!
                  TopAppBarActionType.CLOSE -> HedvigIcons.Close // todo: check here!
                },
                contentDescription = null,
                // todo: to tokens!
              )
            },
          )
          Spacer(Modifier.width(8.dp))
          HedvigText(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp), // todo: to tokens
            style = TypographyKeyTokens.HeadlineSmall.value, // todo: to tokens!
          )
        }
      },
      endSlot = {
        if (topAppBarActions != null) {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            topAppBarActions()
          }
        }
      },
      spaceBetween = 8.dp,
    )
  }
}

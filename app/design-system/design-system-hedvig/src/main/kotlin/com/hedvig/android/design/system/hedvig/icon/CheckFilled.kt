package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.CheckFilled: ImageVector
  get() {
    val current = _checkFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.CheckFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 21.5 a9.5 9.5 0 1 0 0 -19 9.5 9.5 0 0 0 0 19 m4.34 -11.27 a.75 .75 0 0 0 -1.06 -1.06 l-4.35 4.35 a.25 .25 0 0 1 -.35 0 l-1.76 -1.76 a.75 .75 0 1 0 -1.06 1.06 l1.76 1.76 a1.75 1.75 0 0 0 2.47 0z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 21.5
        moveTo(x = 12.0f, y = 21.5f)
        // a 9.5 9.5 0 1 0 0 -19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -19.0f,
        )
        // a 9.5 9.5 0 0 0 0 19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 19.0f,
        )
        // m 4.34 -11.27
        moveToRelative(dx = 4.34f, dy = -11.27f)
        // a 0.75 0.75 0 0 0 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // l -4.35 4.35
        lineToRelative(dx = -4.35f, dy = 4.35f)
        // a 0.25 0.25 0 0 1 -0.35 0
        arcToRelative(
          a = 0.25f,
          b = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.35f,
          dy1 = 0.0f,
        )
        // l -1.76 -1.76
        lineToRelative(dx = -1.76f, dy = -1.76f)
        // a 0.75 0.75 0 1 0 -1.06 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = 1.06f,
        )
        // l 1.76 1.76
        lineToRelative(dx = 1.76f, dy = 1.76f)
        // a 1.75 1.75 0 0 0 2.47 0z
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.47f,
          dy1 = 0.0f,
        )
        close()
      }
    }.build().also { _checkFilled = it }
  }

@Preview
@Composable
private fun IconPreview() {
  com.hedvig.android.design.system.hedvig.HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = CheckFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _checkFilled: ImageVector? = null

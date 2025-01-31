package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.component.tokens.HedvigTypographyKeyTokens
import com.hedvig.android.core.designsystem.theme.SansStandard
import com.hedvig.android.core.designsystem.theme.SerifBookSmall

internal val HedvigTypography: Typography
  @Composable
  get() = Typography(
    displayLarge = MaterialTheme.typography.displayLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 57.sp,
      lineHeight = 64.sp,
      letterSpacing = (-1f).percentage.em,
    ),
    displayMedium = MaterialTheme.typography.displayMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 45.sp,
      lineHeight = 52.sp,
      letterSpacing = (-0.5f).percentage.em,
    ),
    displaySmall = MaterialTheme.typography.displaySmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 36.sp,
      lineHeight = 44.sp,
      letterSpacing = (-0.5f).percentage.em,
    ),
    headlineLarge = MaterialTheme.typography.headlineLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 32.sp,
      lineHeight = 40.sp,
      letterSpacing = (-0.25f).percentage.em,
    ),
    headlineMedium = MaterialTheme.typography.headlineMedium.copy(
      fontFamily = SerifBookSmall,
      fontWeight = FontWeight.Normal,
      fontSize = 28.sp,
      lineHeight = 36.sp,
      letterSpacing = 0f.em,
      lineBreak = LineBreak.Heading,
      textAlign = TextAlign.Center,
    ),
    headlineSmall = MaterialTheme.typography.headlineSmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 24.sp,
      lineHeight = 32.sp,
      letterSpacing = 0f.em,
    ),
    titleLarge = MaterialTheme.typography.titleLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 22.sp,
      lineHeight = 28.sp,
      letterSpacing = 0f.em,
    ),
    titleMedium = MaterialTheme.typography.titleMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.15f.percentage.em,
    ),
    titleSmall = MaterialTheme.typography.titleSmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1f.percentage.em,
    ),
    bodyLarge = MaterialTheme.typography.bodyLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 18.sp, // Changed from 16.sp for the new design system, which isn't reflected in Android UI Kit
      lineHeight = 24.sp,
      letterSpacing = 0.15f.percentage.em,
    ),
    bodyMedium = MaterialTheme.typography.bodyMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25f.percentage.em,
    ),
    bodySmall = MaterialTheme.typography.bodySmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.2f.percentage.em,
    ),
    labelLarge = MaterialTheme.typography.labelLarge.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1f.percentage.em,
    ),
    labelMedium = MaterialTheme.typography.labelMedium.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5f.percentage.em,
    ),
    labelSmall = MaterialTheme.typography.labelSmall.copy(
      fontFamily = SansStandard,
      fontWeight = FontWeight.Normal,
      fontSize = 11.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5f.percentage.em,
    ),
  )

/**
 * Text spacing in figma is specified in a percentage value. To represent that, we need to use "em" which is
 * "relative font size", and use this `percentage` function to use the exact same number as in Figma.
 */
private val Float.percentage: Float
  get() = this / 100

private fun Typography.fromToken(value: HedvigTypographyKeyTokens): TextStyle {
  return when (value) {
    HedvigTypographyKeyTokens.DisplayLarge -> displayLarge
    HedvigTypographyKeyTokens.DisplayMedium -> displayMedium
    HedvigTypographyKeyTokens.DisplaySmall -> displaySmall
    HedvigTypographyKeyTokens.HeadlineLarge -> headlineLarge
    HedvigTypographyKeyTokens.HeadlineMedium -> headlineMedium
    HedvigTypographyKeyTokens.HeadlineSmall -> headlineSmall
    HedvigTypographyKeyTokens.TitleLarge -> titleLarge
    HedvigTypographyKeyTokens.TitleMedium -> titleMedium
    HedvigTypographyKeyTokens.TitleSmall -> titleSmall
    HedvigTypographyKeyTokens.BodyLarge -> bodyLarge
    HedvigTypographyKeyTokens.BodyMedium -> bodyMedium
    HedvigTypographyKeyTokens.BodySmall -> bodySmall
    HedvigTypographyKeyTokens.LabelLarge -> labelLarge
    HedvigTypographyKeyTokens.LabelMedium -> labelMedium
    HedvigTypographyKeyTokens.LabelSmall -> labelSmall
  }
}

@Composable
@ReadOnlyComposable
internal fun HedvigTypographyKeyTokens.toTextStyle(): TextStyle {
  return MaterialTheme.typography.fromToken(this)
}

@Preview
@Composable
private fun PreviewTypography() {
  Surface {
    Column {
      Text("Display Large", style = HedvigTypography.displayLarge)
      Text("Display Medium", style = HedvigTypography.displayMedium)
      Text("Display Small", style = HedvigTypography.displaySmall)
      Text("Headline Large", style = HedvigTypography.headlineLarge)
      Text("Headline Medium", style = HedvigTypography.headlineMedium)
      Text("Headline Small", style = HedvigTypography.headlineSmall)
      Text("Title Large", style = HedvigTypography.titleLarge)
      Text("Title Medium", style = HedvigTypography.titleMedium)
      Text("Title Small", style = HedvigTypography.titleSmall)
      Text("Body Large", style = HedvigTypography.bodyLarge)
      Text("Body Medium", style = HedvigTypography.bodyMedium)
      Text("Body Small", style = HedvigTypography.bodySmall)
      Text("Label Large", style = HedvigTypography.labelLarge)
      Text("Label Medium", style = HedvigTypography.labelMedium)
      Text("Label Small", style = HedvigTypography.labelSmall)
    }
  }
}

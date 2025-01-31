package com.hedvig.android.core.designsystem.component.textfield

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.toTextStyle

@Composable
internal fun HedvigDecorationBox(
  value: String,
  innerTextField: @Composable () -> Unit,
  visualTransformation: VisualTransformation,
  label: @Composable (() -> Unit)?,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  prefix: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  withNewDesign: Boolean = false, // Adapts the TextField to have the big card size and the bigger text size.
  singleLine: Boolean = false,
  enabled: Boolean = true,
  isError: Boolean = false,
  interactionSource: InteractionSource,
  contentPadding: PaddingValues,
  colors: HedvigTextFieldColors,
  container: @Composable () -> Unit,
) {
  val transformedText = remember(value, visualTransformation) {
    visualTransformation.filter(AnnotatedString(value))
  }.text.text

  val isFocused = interactionSource.collectIsFocusedAsState().value
  val inputState = when {
    isFocused -> InputPhase.Focused
    transformedText.isEmpty() -> InputPhase.UnfocusedEmpty
    else -> InputPhase.UnfocusedNotEmpty
  }

  val labelColor: @Composable (InputPhase) -> Color = {
    colors.labelColor(enabled, isError, interactionSource).value
  }

  val typography = MaterialTheme.typography
  // Used for the "small" letters, like when the label shows when the text field is focused.
  val smallTypography = if (withNewDesign) {
    typography.bodyMedium
  } else {
    typography.bodySmall
  }
  // Used for the "big" letters, like when there is any actual input.
  val bigTypography = if (withNewDesign) {
    typography.headlineSmall
  } else {
    typography.bodyLarge
  }

  TextFieldTransitionScope.Transition(
    inputState = inputState,
    focusedTextStyleColor = with(smallTypography.color) {
      this.takeOrElse { labelColor(inputState) }
    },
    unfocusedTextStyleColor = with(bigTypography.color) {
      this.takeOrElse { labelColor(inputState) }
    },
    contentColor = labelColor,
    showLabel = label != null,
  ) { labelProgress, labelTextStyleColor, labelContentColor, placeholderAlphaProgress, prefixSuffixAlphaProgress ->
    val decoratedLabel: @Composable (() -> Unit)? = label?.let {
      @Composable {
        val labelTextStyle = lerp(
          start = bigTypography,
          stop = smallTypography,
          fraction = labelProgress,
        ).let {
          it.copy(color = labelTextStyleColor)
        }
        Decoration(labelContentColor, labelTextStyle, it)
      }
    }

    // Transparent components interfere with Talkback (b/261061240), so if any components below
    // have alpha == 0, we set the component to null instead.

    val decoratedPlaceholder: @Composable ((Modifier) -> Unit)? =
      if (placeholder != null && transformedText.isEmpty() && placeholderAlphaProgress > 0f) {
        @Composable { modifier ->
          Box(modifier.alpha(placeholderAlphaProgress)) {
            Decoration(
              contentColor = colors.placeholderColor(enabled, isError, interactionSource).value,
              typography = smallTypography,
              content = placeholder,
            )
          }
        }
      } else {
        null
      }

    val prefixColor = colors.prefixColor(enabled, isError, interactionSource).value
    val decoratedPrefix: @Composable (() -> Unit)? =
      if (prefix != null && prefixSuffixAlphaProgress > 0f) {
        @Composable {
          Box(Modifier.alpha(prefixSuffixAlphaProgress)) {
            Decoration(
              contentColor = prefixColor,
              typography = smallTypography,
              content = prefix,
            )
          }
        }
      } else {
        null
      }

    val suffixColor = colors.suffixColor(enabled, isError, interactionSource).value
    val decoratedSuffix: @Composable (() -> Unit)? =
      if (suffix != null && prefixSuffixAlphaProgress > 0f) {
        @Composable {
          Box(Modifier.alpha(prefixSuffixAlphaProgress)) {
            Decoration(
              contentColor = suffixColor,
              typography = bigTypography, // Hedvig adjusted to big typography
              content = suffix,
            )
          }
        }
      } else {
        null
      }

    // Developers need to handle invalid input manually. But since we don't provide error
    // message slot API, we can set the default error message in case developers forget about
    // it.
    val defaultErrorMessage = stringResource(androidx.compose.ui.R.string.default_error_message)
    val decorationBoxModifier = Modifier.semantics { if (isError) error(defaultErrorMessage) }

    val leadingIconColor = colors.leadingIconColor(enabled, isError, interactionSource).value
    val decoratedLeading: @Composable (() -> Unit)? = leadingIcon?.let {
      @Composable {
        Decoration(contentColor = leadingIconColor, content = it)
      }
    }

    val trailingIconColor = colors.trailingIconColor(enabled, isError, interactionSource).value
    val decoratedTrailing: @Composable (() -> Unit)? = trailingIcon?.let {
      @Composable {
        Decoration(contentColor = trailingIconColor, content = it)
      }
    }

    val supportingTextColor = colors.supportingTextColor(enabled, isError, interactionSource).value
    val decoratedSupporting: @Composable (() -> Unit)? = supportingText?.let {
      @Composable {
        Decoration(
          contentColor = supportingTextColor,
          typography = HedvigTextFieldTokens.SupportingFont.toTextStyle(),
          content = it,
        )
      }
    }

    val containerWithId: @Composable () -> Unit = {
      Box(
        Modifier.layoutId(ContainerId),
        propagateMinConstraints = true,
      ) {
        container()
      }
    }

    HedvigTextFieldLayout(
      modifier = decorationBoxModifier,
      textField = innerTextField,
      placeholder = decoratedPlaceholder,
      label = decoratedLabel,
      leading = decoratedLeading,
      trailing = decoratedTrailing,
      prefix = decoratedPrefix,
      suffix = decoratedSuffix,
      container = containerWithId,
      supporting = decoratedSupporting,
      singleLine = singleLine,
      animationProgress = labelProgress,
      paddingValues = contentPadding,
    )
  }
}

/**
 * Set content color, typography and emphasis for [content] composable
 */
@Composable
internal fun Decoration(
  contentColor: Color,
  typography: TextStyle? = null,
  content: @Composable () -> Unit,
) {
  val contentWithColor: @Composable () -> Unit = @Composable {
    CompositionLocalProvider(
      LocalContentColor provides contentColor,
      content = content,
    )
  }
  if (typography != null) ProvideTextStyle(typography, contentWithColor) else contentWithColor()
}

internal fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0
internal fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0

private object TextFieldTransitionScope {
  @Composable
  fun Transition(
    inputState: InputPhase,
    focusedTextStyleColor: Color,
    unfocusedTextStyleColor: Color,
    contentColor: @Composable (InputPhase) -> Color,
    showLabel: Boolean,
    content: @Composable (
      labelProgress: Float,
      labelTextStyleColor: Color,
      labelContentColor: Color,
      placeholderOpacity: Float,
      prefixSuffixOpacity: Float,
    ) -> Unit,
  ) {
    // Transitions from/to InputPhase.Focused are the most critical in the transition below.
    // UnfocusedEmpty <-> UnfocusedNotEmpty are needed when a single state is used to control
    // multiple text fields.
    val transition = updateTransition(inputState, label = "TextFieldInputState")

    val labelProgress by transition.animateFloat(
      label = "LabelProgress",
      transitionSpec = { tween(durationMillis = AnimationDuration) },
    ) {
      when (it) {
        InputPhase.Focused -> 1f
        InputPhase.UnfocusedEmpty -> 0f
        InputPhase.UnfocusedNotEmpty -> 1f
      }
    }

    val placeholderOpacity by transition.animateFloat(
      label = "PlaceholderOpacity",
      transitionSpec = {
        if (InputPhase.Focused isTransitioningTo InputPhase.UnfocusedEmpty) {
          tween(
            durationMillis = PlaceholderAnimationDelayOrDuration,
            easing = LinearEasing,
          )
        } else if (InputPhase.UnfocusedEmpty isTransitioningTo InputPhase.Focused ||
          InputPhase.UnfocusedNotEmpty isTransitioningTo InputPhase.UnfocusedEmpty
        ) {
          tween(
            durationMillis = PlaceholderAnimationDuration,
            delayMillis = PlaceholderAnimationDelayOrDuration,
            easing = LinearEasing,
          )
        } else {
          spring()
        }
      },
    ) {
      when (it) {
        InputPhase.Focused -> 1f
        InputPhase.UnfocusedEmpty -> if (showLabel) 0f else 1f
        InputPhase.UnfocusedNotEmpty -> 0f
      }
    }

    val prefixSuffixOpacity by transition.animateFloat(
      label = "PrefixSuffixOpacity",
      transitionSpec = { tween(durationMillis = AnimationDuration) },
    ) {
      when (it) {
        InputPhase.Focused -> 1f
        InputPhase.UnfocusedEmpty -> if (showLabel) 0f else 1f
        InputPhase.UnfocusedNotEmpty -> 1f
      }
    }

    val labelTextStyleColor by transition.animateColor(
      transitionSpec = { tween(durationMillis = AnimationDuration) },
      label = "LabelTextStyleColor",
    ) {
      when (it) {
        InputPhase.Focused -> focusedTextStyleColor
        else -> unfocusedTextStyleColor
      }
    }

    val labelContentColor by transition.animateColor(
      transitionSpec = { tween(durationMillis = AnimationDuration) },
      label = "LabelContentColor",
      targetValueByState = contentColor,
    )

    content(
      labelProgress,
      labelTextStyleColor,
      labelContentColor,
      placeholderOpacity,
      prefixSuffixOpacity,
    )
  }
}

/**
 * An internal state used to animate a label and an indicator.
 */
private enum class InputPhase {
  // Text field is focused
  Focused,

  // Text field is not focused and input text is empty
  UnfocusedEmpty,

  // Text field is not focused but input text is not empty
  UnfocusedNotEmpty,
}

internal val IntrinsicMeasurable.layoutId: Any?
  get() = (parentData as? LayoutIdParentData)?.layoutId

internal const val TextFieldId = "TextField"
internal const val PlaceholderId = "Hint"
internal const val LabelId = "Label"
internal const val LeadingId = "Leading"
internal const val TrailingId = "Trailing"
internal const val PrefixId = "Prefix"
internal const val SuffixId = "Suffix"
internal const val SupportingId = "Supporting"
internal const val ContainerId = "Container"
internal val ZeroConstraints = Constraints(0, 0, 0, 0)

internal const val SignalAnimationDuration = 400L // TODO double check with web on animation specs
internal const val AnimationDuration = 150
private const val PlaceholderAnimationDuration = 83
private const val PlaceholderAnimationDelayOrDuration = 67

internal val TextFieldPadding = 16.dp
internal val HorizontalIconPadding = 12.dp
internal val SupportingHorizontalPadding = 4.dp
internal val SupportingTopPadding = 4.dp
internal val PrefixSuffixTextPadding = 2.dp
internal val MinTextLineHeight = 24.dp
internal val MinFocusedLabelLineHeight = 16.dp
internal val MinSupportingTextLineHeight = 16.dp

internal val IconDefaultSizeModifier = Modifier.defaultMinSize(48.dp, 48.dp)

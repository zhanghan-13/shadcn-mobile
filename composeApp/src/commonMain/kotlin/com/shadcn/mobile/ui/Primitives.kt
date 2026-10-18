package com.shadcn.mobile.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ButtonVariant { Primary, Outline, Ghost }

@Composable
fun SButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = ShadcnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val active = enabled && !loading
    val fill by animateColorAsState(
        targetValue = when {
            variant == ButtonVariant.Primary && pressed -> colors.primary.copy(alpha = 0.82f)
            variant == ButtonVariant.Primary -> colors.primary
            pressed || focused -> colors.muted
            variant == ButtonVariant.Outline -> colors.surface
            else -> Color.Transparent
        },
        animationSpec = tween(120),
        label = "button-fill",
    )
    val foreground = if (variant == ButtonVariant.Primary) colors.onPrimary else colors.foreground
    val shape = RoundedCornerShape(8.dp)
    // Guard semantic activation too, including while a loading button is inactive.
    Row(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(shape)
            .background(fill)
            .border(if (focused) 2.dp else 1.dp, if (focused) colors.mutedForeground else if (variant == ButtonVariant.Outline) colors.border else Color.Transparent, shape)
            .clickable(enabled = active, role = Role.Button, interactionSource = interaction, indication = LocalIndication.current,
                onClick = { if (active) onClick() })
            .semantics { if (loading) stateDescription = "正在处理" }
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides foreground) {
            if (loading) {
                LoadingIndicator()
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = foreground)
            if (trailingIcon != null && !loading) {
                Spacer(Modifier.width(10.dp))
                trailingIcon()
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    val color = LocalContentColor.current
    val rotation by rememberInfiniteTransition(label = "loading").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "loading-rotation",
    )
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        rotate(rotation) {
            drawArc(color, 0f, 270f, useCenter = false, topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()), style = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
fun SInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    testTag: String = "",
) {
    val colors = ShadcnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val edge by animateColorAsState(
        if (error != null) colors.error else if (focused) colors.foreground else colors.input,
        animationSpec = tween(120), label = "input-edge",
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, Modifier.clearAndSetSemantics {}, color = colors.foreground, style = MaterialTheme.typography.labelLarge)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
                .semantics {
                    contentDescription = label
                    if (error != null) error(error)
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.foreground),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(colors.foreground),
            interactionSource = interaction,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                        .background(colors.surface, RoundedCornerShape(8.dp))
                        .border(if (focused) 2.dp else 1.dp, edge, RoundedCornerShape(8.dp))
                        .padding(horizontal = 13.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) Text(placeholder, Modifier.clearAndSetSemantics {}, style = MaterialTheme.typography.bodyLarge,
                        color = colors.mutedForeground, maxLines = if (singleLine) 1 else Int.MAX_VALUE, overflow = TextOverflow.Ellipsis)
                    innerTextField()
                }
            },
        )
        if (error != null) {
            Text(error, Modifier.then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_error") else Modifier).semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodySmall, color = colors.error)
        }
    }
}

@Composable
fun SCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    description: String? = null,
    error: String? = null,
    modifier: Modifier = Modifier,
    testTag: String = "",
    enabled: Boolean = true,
) {
    val colors = ShadcnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier = modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed || focused) colors.muted else Color.Transparent)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, interactionSource = interaction, indication = LocalIndication.current,
                onValueChange = { if (enabled) onCheckedChange(it) })
            .semantics(mergeDescendants = true) { if (error != null) error(error) }
            .padding(vertical = 12.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Canvas(Modifier.padding(top = 1.dp).size(20.dp).clearAndSetSemantics {}) {
            val corner = CornerRadius(4.dp.toPx())
            if (checked) {
                drawRoundRect(colors.primary, cornerRadius = corner)
                val path = Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.51f)
                    lineTo(size.width * 0.43f, size.height * 0.69f)
                    lineTo(size.width * 0.76f, size.height * 0.34f)
                }
                drawPath(path, colors.onPrimary, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            } else {
                val inset = 0.75.dp.toPx()
                drawRoundRect(color = if (error != null) colors.error else colors.mutedForeground, topLeft = Offset(inset, inset), size = Size(size.width - 2 * inset, size.height - 2 * inset), cornerRadius = corner, style = Stroke(width = 1.5.dp.toPx()))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = colors.foreground)
            if (description != null) Text(description, style = MaterialTheme.typography.bodySmall, color = colors.mutedForeground)
            if (error != null) Text(error, Modifier.then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_error") else Modifier).semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodySmall, color = colors.error)
        }
    }
}

/** Decorative line icons; provide the accessible label on the containing control. */
@Composable
fun SIcon(name: String, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    val color = LocalContentColor.current
    Canvas(modifier.size(size).clearAndSetSemantics {}) {
        val scale = this.size.width / 24f
        val stroke = Stroke(width = 1.7f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(color, Offset(x1 * scale, y1 * scale), Offset(x2 * scale, y2 * scale), strokeWidth = stroke.width, cap = StrokeCap.Round)
        fun poly(vararg points: Float) {
            val path = Path().apply {
                moveTo(points[0] * scale, points[1] * scale)
                for (i in 2 until points.size step 2) lineTo(points[i] * scale, points[i + 1] * scale)
            }
            drawPath(path, color, style = stroke)
        }
        fun circle(x: Float, y: Float, radius: Float) = drawCircle(color, radius * scale, Offset(x * scale, y * scale), style = stroke)
        when (name) {
            "mark" -> { line(4f, 20f, 20f, 4f); line(13f, 20f, 20f, 13f) }
            "arrow-right" -> { line(4f, 12f, 20f, 12f); poly(14f, 6f, 20f, 12f, 14f, 18f) }
            "chevron-down" -> poly(6f, 9f, 12f, 15f, 18f, 9f)
            "chevron-right" -> poly(9f, 6f, 15f, 12f, 9f, 18f)
            "check" -> poly(5f, 12f, 10f, 17f, 19f, 7f)
            "x" -> { line(6f, 6f, 18f, 18f); line(18f, 6f, 6f, 18f) }
            "plus" -> { line(12f, 5f, 12f, 19f); line(5f, 12f, 19f, 12f) }
            "search" -> { circle(10.5f, 10.5f, 6.5f); line(15.5f, 15.5f, 20f, 20f) }
            "sliders", "settings" -> {
                line(5f, 4f, 5f, 8f); line(5f, 12f, 5f, 20f); circle(5f, 10f, 2f)
                line(12f, 4f, 12f, 13f); line(12f, 17f, 12f, 20f); circle(12f, 15f, 2f)
                line(19f, 4f, 19f, 6f); line(19f, 10f, 19f, 20f); circle(19f, 8f, 2f)
            }
            "sun" -> {
                circle(12f, 12f, 4f)
                line(12f, 2f, 12f, 4f); line(12f, 20f, 12f, 22f)
                line(2f, 12f, 4f, 12f); line(20f, 12f, 22f, 12f)
                line(4.9f, 4.9f, 6.3f, 6.3f); line(17.7f, 17.7f, 19.1f, 19.1f)
                line(4.9f, 19.1f, 6.3f, 17.7f); line(17.7f, 6.3f, 19.1f, 4.9f)
            }
            "moon" -> {
                val path = Path().apply {
                    moveTo(20.5f * scale, 13f * scale)
                    cubicTo(18.5f * scale, 14f * scale, 15.5f * scale, 14f * scale, 13f * scale, 11.5f * scale)
                    cubicTo(10.5f * scale, 9f * scale, 10.5f * scale, 6f * scale, 11.5f * scale, 3.5f * scale)
                    cubicTo(5f * scale, 3.8f * scale, 1.5f * scale, 11.7f * scale, 6.3f * scale, 17.4f * scale)
                    cubicTo(10.7f * scale, 22.5f * scale, 19.2f * scale, 20.6f * scale, 20.5f * scale, 13f * scale)
                    close()
                }
                drawPath(path, color, style = stroke)
            }
        }
    }
}

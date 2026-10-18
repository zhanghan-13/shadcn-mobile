package com.shadcn.mobile.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun SSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "",
) {
    val colors = ShadcnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val thumbPosition by animateFloatAsState(
        if (checked != rtl) 1f else 0f, tween(140), label = "switch-thumb",
    )
    val track by animateColorAsState(
        if (checked) colors.primary else colors.input, tween(140), label = "switch-track",
    )
    Row(
        modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (focused || pressed) colors.muted else Color.Transparent)
            .border(1.dp, if (focused) colors.foreground else Color.Transparent, RoundedCornerShape(8.dp))
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .toggleable(checked, interactionSource = interaction, indication = LocalIndication.current,
                enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = colors.foreground)
            if (description != null) Text(description, style = MaterialTheme.typography.bodySmall, color = colors.mutedForeground)
        }
        Canvas(Modifier.size(42.dp, 24.dp).clearAndSetSemantics {}) {
            drawRoundRect(track, cornerRadius = CornerRadius(size.height / 2))
            val inset = 3.dp.toPx()
            val radius = size.height / 2 - inset
            val start = inset + radius
            val end = size.width - inset - radius
            drawCircle(if (checked) colors.onPrimary else colors.surface, radius,
                center = Offset(start + (end - start) * thumbPosition, size.height / 2))
        }
    }
}

@Composable
fun STextarea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    minLines: Int = 3,
    maxLines: Int = 6,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    testTag: String = "",
) {
    require(minLines > 0 && maxLines >= minLines) { "Textarea requires 1 <= minLines <= maxLines" }
    val colors = ShadcnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val edge by animateColorAsState(
        if (error != null) colors.error else if (focused) colors.foreground else colors.input,
        tween(120), label = "textarea-edge",
    )
    Column(modifier.alpha(if (enabled) 1f else 0.45f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, Modifier.clearAndSetSemantics {}, style = MaterialTheme.typography.labelLarge, color = colors.foreground)
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
            enabled = enabled,
            readOnly = readOnly,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.foreground),
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(colors.foreground),
            interactionSource = interaction,
            decorationBox = { innerTextField ->
                Box(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                        .background(colors.surface, RoundedCornerShape(8.dp))
                        .border(if (focused) 2.dp else 1.dp, edge, RoundedCornerShape(8.dp))
                        .padding(horizontal = 13.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (value.isEmpty()) Text(placeholder, Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.bodyLarge, color = colors.mutedForeground,
                        maxLines = maxLines, overflow = TextOverflow.Ellipsis)
                    innerTextField()
                }
            },
        )
        if (error != null) Text(
            error,
            Modifier.then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_error") else Modifier)
                .semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.bodySmall, color = colors.error,
        )
    }
}

data class RadioOption(
    val id: String,
    val label: String,
    val description: String? = null,
    val enabled: Boolean = true,
)

@Composable
fun SRadioGroup(
    options: List<RadioOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    testTag: String = "",
) {
    require(options.map { it.id }.distinct().size == options.size) { "Radio option IDs must be unique" }
    val colors = ShadcnTheme.colors
    Column(modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.foreground)
        options.forEach { option ->
            val active = enabled && option.enabled
            val selected = selectedId == option.id
            val interaction = remember(option.id) { MutableInteractionSource() }
            val focused by interaction.collectIsFocusedAsState()
            val pressed by interaction.collectIsPressedAsState()
            Row(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    .alpha(if (active) 1f else 0.45f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (focused || pressed) colors.muted else Color.Transparent)
                    .border(1.dp, if (focused) colors.foreground else Color.Transparent, RoundedCornerShape(8.dp))
                    .then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_${option.id}") else Modifier)
                    .selectable(selected, interactionSource = interaction, indication = LocalIndication.current,
                        enabled = active, role = Role.RadioButton, onClick = { onSelect(option.id) })
                    .semantics { if (error != null) error(error) }
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Canvas(Modifier.padding(top = 1.dp).size(20.dp).clearAndSetSemantics {}) {
                    drawCircle(if (error != null) colors.error else if (selected) colors.primary else colors.mutedForeground,
                        radius = size.minDimension / 2 - 1.dp.toPx(), style = Stroke(1.5.dp.toPx()))
                    if (selected) drawCircle(colors.primary, radius = 5.dp.toPx())
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(option.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = colors.foreground)
                    if (option.description != null) Text(option.description,
                        style = MaterialTheme.typography.bodySmall, color = colors.mutedForeground)
                }
            }
        }
        if (error != null) Text(
            error,
            Modifier.then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_error") else Modifier)
                .semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.bodySmall, color = colors.error,
        )
    }
}

enum class AlertVariant { Default, Destructive }

@Composable
fun SAlert(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    variant: AlertVariant = AlertVariant.Default,
    announce: Boolean = false,
) {
    val colors = ShadcnTheme.colors
    val destructive = variant == AlertVariant.Destructive
    val foreground = if (destructive) colors.error else colors.foreground
    Column(
        modifier.fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(8.dp))
            .border(1.dp, if (destructive) colors.error else colors.border, RoundedCornerShape(8.dp))
            .semantics(mergeDescendants = true) { if (announce) liveRegion = LiveRegionMode.Polite }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = foreground)
        if (description != null) Text(description, style = MaterialTheme.typography.bodySmall,
            color = if (destructive) foreground else colors.mutedForeground)
    }
}

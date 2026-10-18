package com.shadcn.mobile.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.roundToInt

data class TabOption(val id: String, val label: String, val enabled: Boolean = true)

/** A controlled tab list. Saveable state inside each panel survives changing tabs. */
@Composable
fun STabs(
    options: List<TabOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "",
    content: @Composable (String) -> Unit,
) {
    require(options.map { it.id }.distinct().size == options.size) { "Tab IDs must be unique" }
    val colors = ShadcnTheme.colors
    val panelState = rememberSaveableStateHolder()
    val focusRequesters = remember(options.map { it.id }) { options.associate { it.id to FocusRequester() } }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val enabledOptions = options.filter { it.enabled }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup()
                .background(colors.muted, RoundedCornerShape(10.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEach { option ->
                key(option.id) {
                    val active = enabled && option.enabled
                    val selected = selectedId == option.id
                    val interaction = remember { MutableInteractionSource() }
                    val focused by interaction.collectIsFocusedAsState()
                    val pressed by interaction.collectIsPressedAsState()
                    Box(
                        Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .alpha(if (active) 1f else 0.45f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) colors.surface else if (pressed) colors.input else Color.Transparent)
                            .border(1.dp, if (focused) colors.foreground else Color.Transparent, RoundedCornerShape(6.dp))
                            .then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_${option.id}") else Modifier)
                            .focusRequester(focusRequesters.getValue(option.id))
                            .onPreviewKeyEvent { event ->
                                if (!active || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                                val index = enabledOptions.indexOfFirst { it.id == option.id }
                                val destination = when (event.key) {
                                    Key.DirectionRight -> enabledOptions[(index + if (rtl) enabledOptions.size - 1 else 1) % enabledOptions.size]
                                    Key.DirectionLeft -> enabledOptions[(index + if (rtl) 1 else enabledOptions.size - 1) % enabledOptions.size]
                                    Key.MoveHome -> enabledOptions.first()
                                    Key.MoveEnd -> enabledOptions.last()
                                    else -> return@onPreviewKeyEvent false
                                }
                                focusRequesters.getValue(destination.id).requestFocus()
                                if (destination.id != selectedId) onSelect(destination.id)
                                true
                            }
                            .selectable(selected, interactionSource = interaction, indication = LocalIndication.current,
                                enabled = active, role = Role.Tab,
                                onClick = { if (active && !selected) onSelect(option.id) })
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(option.label, color = if (selected) colors.foreground else colors.mutedForeground,
                            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        options.firstOrNull { it.id == selectedId }?.let { selected ->
            panelState.SaveableStateProvider(selected.id) {
                Box(Modifier.fillMaxWidth().then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_content") else Modifier)) {
                    content(selected.id)
                }
            }
        }
    }
}

data class AccordionItem(val id: String, val title: String, val enabled: Boolean = true)

/** A controlled single-open accordion. A collapsed panel is absent from the accessibility tree. */
@Composable
fun SAccordion(
    items: List<AccordionItem>,
    expandedId: String?,
    onExpandedChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    collapsible: Boolean = true,
    testTag: String = "",
    content: @Composable (String) -> Unit,
) {
    require(items.map { it.id }.distinct().size == items.size) { "Accordion IDs must be unique" }
    val colors = ShadcnTheme.colors
    val panelState = rememberSaveableStateHolder()
    Column(modifier.fillMaxWidth()) {
        items.forEach { item ->
            key(item.id) {
                val active = enabled && item.enabled
                val expanded = expandedId == item.id
                val interaction = remember { MutableInteractionSource() }
                val focused by interaction.collectIsFocusedAsState()
                val pressed by interaction.collectIsPressedAsState()
                val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(140), label = "accordion-chevron")
                Row(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                        .alpha(if (active) 1f else 0.45f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (pressed || focused) colors.muted else Color.Transparent)
                        .border(1.dp, if (focused) colors.foreground else Color.Transparent, RoundedCornerShape(6.dp))
                        .then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_${item.id}") else Modifier)
                        .clickable(enabled = active, role = Role.Button, interactionSource = interaction,
                            indication = LocalIndication.current, onClick = {
                                if (active && (!expanded || collapsible)) onExpandedChange(if (expanded) null else item.id)
                            })
                        .semantics { stateDescription = if (expanded) "已展开" else "已折叠" }
                        .padding(horizontal = 4.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(item.title, Modifier.weight(1f), color = colors.foreground,
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    SIcon("chevron-down", Modifier.rotate(rotation))
                }
                if (expanded) {
                    panelState.SaveableStateProvider(item.id) {
                        Box(Modifier.fillMaxWidth()
                            .then(if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_${item.id}_content") else Modifier)
                            .padding(start = 4.dp, end = 4.dp, bottom = 16.dp)) {
                            content(item.id)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border).clearAndSetSemantics {})
            }
        }
    }
}

/** Native slider gestures, keyboard controls and adjustable semantics with shadcn styling. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    testTag: String = "",
) {
    require(value.isFinite()) { "Slider value must be finite" }
    require(valueRange.start.isFinite() && valueRange.endInclusive.isFinite() && valueRange.start < valueRange.endInclusive) {
        "Slider requires a finite, increasing value range"
    }
    require(steps >= 0) { "Slider steps must not be negative" }
    val colors = ShadcnTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val safeValue = snappedSliderValue(value, valueRange, steps)
    Column(modifier.alpha(if (enabled) 1f else 0.45f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, Modifier.clearAndSetSemantics {}, color = colors.foreground, style = MaterialTheme.typography.labelLarge)
        Slider(
            value = safeValue,
            onValueChange = { candidate ->
                if (enabled && candidate.isFinite()) {
                    // Also normalize accessibility adjustments: native snapping differs for negative ranges.
                    val next = snappedSliderValue(candidate, valueRange, steps)
                    if (next != safeValue) onValueChange(next)
                }
            },
            onValueChangeFinished = { if (enabled) onValueChangeFinished?.invoke() },
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                .border(1.dp, if (focused) colors.foreground else Color.Transparent, RoundedCornerShape(8.dp))
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
                .semantics {
                    contentDescription = label
                    // Native accessibility bridges can still invoke actions on a disabled node.
                    if (!enabled) setProgress { false }
                },
            thumb = {
                // Slider measures its semantic and gesture bounds from the thumb/track slots.
                // Keep the thumb visual small without shrinking the native control to 20 dp.
                Box(Modifier.size(width = 20.dp, height = 48.dp).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(20.dp)) {
                        val radius = size.minDimension / 2 - 1.dp.toPx()
                        drawCircle(if (pressed) colors.muted else colors.surface, radius)
                        drawCircle(colors.primary, radius, style = Stroke(2.dp.toPx()))
                    }
                }
            },
            track = { state ->
                Canvas(Modifier.fillMaxWidth().height(6.dp).clearAndSetSemantics {}) {
                    val fraction = ((state.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
                    val activeWidth = size.width * fraction
                    drawRoundRect(colors.input, cornerRadius = CornerRadius(size.height / 2))
                    if (activeWidth > 0f) drawRoundRect(colors.primary,
                        topLeft = Offset(if (layoutDirection == LayoutDirection.Rtl) size.width - activeWidth else 0f, 0f),
                        size = Size(activeWidth, size.height), cornerRadius = CornerRadius(size.height / 2))
                }
            },
        )
    }
}

private fun snappedSliderValue(value: Float, range: ClosedFloatingPointRange<Float>, steps: Int): Float {
    val bounded = value.coerceIn(range)
    if (steps == 0) return bounded
    val start = range.start.toDouble()
    val span = range.endInclusive.toDouble() - start
    val intervals = steps.toDouble() + 1.0
    val tick = floor((bounded.toDouble() - start) / span * intervals + 0.5)
    return (start + span * tick / intervals).toFloat().coerceIn(range)
}

/** Null means progress is unknown; its static striped segment never represents a percentage. */
@Composable
fun SProgress(
    value: Float?,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
    require(value == null || value.isFinite()) { "Progress value must be finite or null" }
    val colors = ShadcnTheme.colors
    val progress = value?.coerceIn(0f, 1f)
    val description = if (progress == null) "进度未定" else "${(progress * 100).roundToInt()}%"
    Column(
        modifier.fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .semantics(mergeDescendants = true) {
                contentDescription = label
                stateDescription = description
                progressBarRangeInfo = if (progress == null) ProgressBarRangeInfo.Indeterminate else ProgressBarRangeInfo(progress, 0f..1f)
            },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(label, Modifier.weight(1f), color = colors.foreground, style = MaterialTheme.typography.labelLarge)
            Text(description, color = colors.mutedForeground, style = MaterialTheme.typography.bodySmall)
        }
        Canvas(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).clearAndSetSemantics {}) {
            drawRoundRect(colors.input, cornerRadius = CornerRadius(size.height / 2))
            if (progress != null) {
                val activeWidth = size.width * progress
                if (activeWidth > 0f) drawRoundRect(colors.primary,
                    topLeft = Offset(if (layoutDirection == LayoutDirection.Rtl) size.width - activeWidth else 0f, 0f),
                    size = Size(activeWidth, size.height), cornerRadius = CornerRadius(size.height / 2))
            } else {
                val segmentStart = size.width * 0.35f
                val segmentEnd = size.width * 0.65f
                clipRect(left = segmentStart, right = segmentEnd) {
                    var x = segmentStart - size.height
                    while (x < segmentEnd) {
                        drawLine(colors.primary, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 3.dp.toPx())
                        x += 8.dp.toPx()
                    }
                }
            }
        }
    }
}

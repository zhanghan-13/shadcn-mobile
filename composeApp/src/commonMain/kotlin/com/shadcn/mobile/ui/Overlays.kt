package com.shadcn.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shadcn.mobile.form.Roles
import com.shadcn.mobile.form.filterRoles
import kotlinx.coroutines.launch

@Composable
fun SCombobox(selectedId: String, onSelect: (String) -> Unit, error: String? = null, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val colors = ShadcnTheme.colors
    val selected = Roles.firstOrNull { it.id == selectedId }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val triggerFocus = remember { FocusRequester() }
    fun dismiss() { expanded = false; keyboard?.hide(); triggerFocus.requestFocus() }
    Column(modifier.fillMaxWidth()) {
        Text("你的角色", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.foreground)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("role_trigger").focusRequester(triggerFocus)
                    .border(1.dp, if (error != null) colors.error else colors.border, RoundedCornerShape(8.dp))
                    .semantics {
                        contentDescription = "你的角色"
                        stateDescription = if (expanded) "已展开" else "已收起"
                        if (error != null) error(error)
                    }
                    .clickable(role = Role.DropdownList) { focusManager.clearFocus(); keyboard?.hide(); query = ""; expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(selected?.title ?: "选择你的角色", Modifier.weight(1f), fontSize = 15.sp,
                    color = if (selected == null) colors.mutedForeground else colors.foreground)
                SIcon("chevron-down", size = 17.dp)
            }
            DropdownMenu(
                expanded = expanded, onDismissRequest = ::dismiss,
                modifier = Modifier.widthIn(min = 280.dp, max = 340.dp).heightIn(max = 390.dp).testTag("role_menu"),
                shape = RoundedCornerShape(10.dp), containerColor = colors.surface,
                tonalElevation = 0.dp, shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    SInput(query, { query = it }, label = "搜索角色", placeholder = "搜索中文或英文…", testTag = "role_search")
                    Spacer(Modifier.height(8.dp))
                    val options = filterRoles(query)
                    if (options.isEmpty()) {
                        Text("没有匹配的角色，试试其他关键词。", Modifier.testTag("role_empty").padding(vertical = 20.dp),
                            color = colors.mutedForeground, fontSize = 13.sp)
                    }
                    options.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("role_option_${option.id}")
                                .background(if (selectedId == option.id) colors.muted else colors.surface, RoundedCornerShape(6.dp))
                                .selectable(selected = selectedId == option.id, role = Role.RadioButton, onClick = { onSelect(option.id); dismiss() })
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(option.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.foreground)
                                Text(option.description, fontSize = 11.sp, lineHeight = 17.sp, color = colors.mutedForeground)
                            }
                            if (selectedId == option.id) SIcon("check", size = 16.dp)
                        }
                    }
                }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Text(error, Modifier.testTag("error_role").semantics { error(error); liveRegion = LiveRegionMode.Polite },
                fontSize = 12.sp, lineHeight = 18.sp, color = colors.error)
        }
    }
}

@Composable
fun SDialog(title: String, description: String, onDismiss: () -> Unit, tag: String = "dialog", content: @Composable ColumnScope.() -> Unit) {
    val colors = ShadcnTheme.colors
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.padding(horizontal = 24.dp).widthIn(max = 440.dp).fillMaxWidth()
                .heightIn(max = 680.dp).imePadding().testTag(tag).semantics { paneTitle = title }
                .background(colors.surface, RoundedCornerShape(12.dp)).border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            Text(title, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, color = colors.foreground,
                modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(8.dp))
            Text(description, fontSize = 14.sp, lineHeight = 22.sp, color = colors.mutedForeground)
            Spacer(Modifier.height(22.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesSheet(initialEmailUpdates: Boolean, initialProductNews: Boolean, onDismiss: () -> Unit, onSave: (Boolean, Boolean) -> Unit) {
    var emailUpdates by rememberSaveable { mutableStateOf(initialEmailUpdates) }
    var productNews by rememberSaveable { mutableStateOf(initialProductNews) }
    val colors = ShadcnTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = state, containerColor = colors.surface,
        contentColor = colors.foreground, tonalElevation = 0.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp, bottom = 6.dp).size(36.dp, 4.dp).testTag("preferences_drag_handle").background(colors.border, RoundedCornerShape(2.dp))) },
    ) {
        Column(Modifier.fillMaxWidth().testTag("preferences_sheet").verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp).padding(top = 18.dp, bottom = 24.dp)) {
            Text("只接收你关心的内容", fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(8.dp))
            Text("随时调整通知。保存后会应用到你的工作空间。", fontSize = 14.sp, lineHeight = 22.sp, color = colors.mutedForeground)
            Spacer(Modifier.height(24.dp))
            SCheckbox(emailUpdates, { emailUpdates = it }, "工作空间更新", "成员邀请、协作进展与重要变更。", testTag = "email_updates")
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = colors.border)
            Spacer(Modifier.height(12.dp))
            SCheckbox(productNews, { productNews = it }, "产品动态", "新功能发布与实用技巧，偶尔来信。", testTag = "product_news")
            Spacer(Modifier.height(28.dp))
            SButton("保存偏好", {
                scope.launch { state.hide(); onSave(emailUpdates, productNews) }
            }, Modifier.fillMaxWidth().testTag("save_preferences"))
            Spacer(Modifier.height(8.dp))
            SButton("取消", {
                scope.launch { state.hide(); onDismiss() }
            }, Modifier.fillMaxWidth().testTag("cancel_preferences"), ButtonVariant.Ghost)
        }
    }
}

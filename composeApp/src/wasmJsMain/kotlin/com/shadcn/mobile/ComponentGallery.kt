package com.shadcn.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadcn.mobile.form.Roles
import com.shadcn.mobile.form.FormValues
import com.shadcn.mobile.form.validateForm
import com.shadcn.mobile.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val componentNames = setOf("button", "input", "checkbox", "combobox", "dialog", "sheet")

/** Browser-only examples call the exact component implementations used by Android and iOS. */
@Composable
internal fun ComponentGallery(component: String, darkTheme: Boolean?) {
    ShadcnTheme(darkTheme = darkTheme ?: isSystemInDarkTheme()) {
        val colors = ShadcnTheme.colors
        Box(Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(component.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold, color = colors.foreground)
                when (component) {
                    "button" -> ButtonExample()
                    "input" -> InputExample()
                    "checkbox" -> CheckboxExample()
                    "combobox" -> ComboboxExample()
                    "dialog" -> DialogExample()
                    "sheet" -> SheetExample()
                }
            }
        }
    }
}

@Composable
private fun ButtonExample() {
    var clicks by remember { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    SButton("Primary · 点击 $clicks 次", { clicks++ }, Modifier.fillMaxWidth())
    SButton("Outline · 点击试试", { clicks++ }, Modifier.fillMaxWidth(), variant = ButtonVariant.Outline)
    SButton("Ghost · 点击试试", { clicks++ }, Modifier.fillMaxWidth(), variant = ButtonVariant.Ghost)
    SButton(if (saved) "已保存，可以再次体验" else "体验加载状态", {
        saving = true
        saved = false
        scope.launch { delay(900); saving = false; saved = true }
    }, Modifier.fillMaxWidth(), loading = saving)
    SButton("Disabled", {}, Modifier.fillMaxWidth(), enabled = false)
}

@Composable
private fun InputExample() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    SInput(name, { name = it }, "工作空间名称", placeholder = "例如：设计工作室")
    SInput(email, { email = it }, "工作邮箱", placeholder = "you@example.com",
        error = if (checked) validateForm(FormValues(email = email)).email else null)
    SButton("验证输入", { checked = true }, Modifier.fillMaxWidth(), variant = ButtonVariant.Outline)
    ExampleNote("支持中文输入、焦点边框和错误提示。已输入 ${name.length} 个字符。")
}

@Composable
private fun CheckboxExample() {
    var updates by remember { mutableStateOf(true) }
    var terms by remember { mutableStateOf(false) }
    var confirmed by remember { mutableStateOf(false) }
    SCheckbox(updates, { updates = it }, "工作空间更新", "点击整行即可切换，保留至少 48dp 的交互区域。")
    SCheckbox(terms, { terms = it; confirmed = false }, "同意演示条款", "选中后，下方按钮可用。")
    SCheckbox(false, {}, "不可用的选项", "演示禁用状态。", enabled = false)
    SButton(if (confirmed) "已确认" else if (terms) "已同意，可以继续" else "请先同意演示条款",
        { confirmed = true }, Modifier.fillMaxWidth(), enabled = terms)
    if (confirmed) ExampleNote("已确认演示条款。取消勾选后可再次体验。")
}

@Composable
private fun ComboboxExample() {
    var selected by remember { mutableStateOf("") }
    SCombobox(selectedId = selected, onSelect = { selected = it })
    ExampleNote(Roles.firstOrNull { it.id == selected }?.let { "已选择：${it.title}" } ?: "展开后试着搜索“开发”或“designer”，也可以输入不存在的角色。")
}

@Composable
private fun DialogExample() {
    var visible by remember { mutableStateOf(false) }
    var confirmed by remember { mutableStateOf(false) }
    SButton("打开确认对话框", { visible = true }, Modifier.fillMaxWidth())
    ExampleNote(if (confirmed) "已确认本地演示操作。" else "可用取消按钮、Escape 或点击遮罩关闭。")
    if (visible) {
        SDialog("确认创建工作空间？", "这是本地交互演示，不会创建在线账户。", onDismiss = { visible = false }) {
            SButton("确认创建", { confirmed = true; visible = false }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            SButton("取消", { visible = false }, Modifier.fillMaxWidth(), variant = ButtonVariant.Outline)
        }
    }
}

@Composable
private fun SheetExample() {
    var visible by remember { mutableStateOf(false) }
    var updates by remember { mutableStateOf(true) }
    var news by remember { mutableStateOf(false) }
    SButton("调整通知偏好", { visible = true }, Modifier.fillMaxWidth(), trailingIcon = { SIcon("sliders") })
    ExampleNote("工作空间更新：${if (updates) "开启" else "关闭"} · 产品动态：${if (news) "开启" else "关闭"}")
    ExampleNote("修改后保存才会应用；取消或关闭面板会丢弃本次修改。")
    if (visible) {
        PreferencesSheet(updates, news, onDismiss = { visible = false }, onSave = { nextUpdates, nextNews ->
            updates = nextUpdates
            news = nextNews
            visible = false
        })
    }
}

@Composable
private fun ExampleNote(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = ShadcnTheme.colors.mutedForeground)
}

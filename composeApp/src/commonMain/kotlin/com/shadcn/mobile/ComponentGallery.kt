package com.shadcn.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadcn.mobile.form.Roles
import com.shadcn.mobile.form.FormValues
import com.shadcn.mobile.form.validateForm
import com.shadcn.mobile.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val componentNames = setOf("button", "input", "checkbox", "combobox", "dialog", "sheet", "switch", "textarea", "radio-group", "alert")

internal fun componentTitle(name: String): String =
    if (name == "radio-group") "RadioGroup" else name.replaceFirstChar { it.uppercase() }

/** One set of interactive examples for Android, iOS, and the browser documentation. */
@Composable
internal fun ComponentGallery(component: String, darkTheme: Boolean?, centerContent: Boolean = true) {
    ShadcnTheme(darkTheme = darkTheme ?: isSystemInDarkTheme()) {
        val colors = ShadcnTheme.colors
        Box(Modifier.fillMaxSize().background(colors.background).safeDrawingPadding().imePadding(),
            contentAlignment = if (centerContent) Alignment.Center else Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().testTag("component_scroll")
                    .verticalScroll(rememberScrollState()).padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(componentTitle(component), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold, color = colors.foreground)
                when (component) {
                    "button" -> ButtonExample()
                    "input" -> InputExample()
                    "checkbox" -> CheckboxExample()
                    "combobox" -> ComboboxExample()
                    "dialog" -> DialogExample()
                    "sheet" -> SheetExample()
                    "switch" -> SwitchExample()
                    "textarea" -> TextareaExample()
                    "radio-group" -> RadioGroupExample()
                    "alert" -> AlertExample()
                }
            }
        }
    }
}

@Composable
private fun ButtonExample() {
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
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
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var checked by rememberSaveable { mutableStateOf(false) }
    SInput(name, { name = it }, "工作空间名称", placeholder = "例如：设计工作室")
    SInput(email, { email = it }, "工作邮箱", placeholder = "you@example.com",
        error = if (checked) validateForm(FormValues(email = email)).email else null)
    SButton("验证输入", { checked = true }, Modifier.fillMaxWidth(), variant = ButtonVariant.Outline)
    ExampleNote("支持中文输入、焦点边框和错误提示。已输入 ${name.length} 个字符。")
}

@Composable
private fun CheckboxExample() {
    var updates by rememberSaveable { mutableStateOf(true) }
    var terms by rememberSaveable { mutableStateOf(false) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    SCheckbox(updates, { updates = it }, "工作空间更新", "点击整行即可切换，保留至少 48dp 的交互区域。")
    SCheckbox(terms, { terms = it; confirmed = false }, "同意演示条款", "选中后，下方按钮可用。")
    SCheckbox(false, {}, "不可用的选项", "演示禁用状态。", enabled = false)
    SButton(if (confirmed) "已确认" else if (terms) "已同意，可以继续" else "请先同意演示条款",
        { confirmed = true }, Modifier.fillMaxWidth(), enabled = terms)
    if (confirmed) ExampleNote("已确认演示条款。取消勾选后可再次体验。")
}

@Composable
private fun ComboboxExample() {
    var selected by rememberSaveable { mutableStateOf("") }
    SCombobox(selectedId = selected, onSelect = { selected = it })
    ExampleNote(Roles.firstOrNull { it.id == selected }?.let { "已选择：${it.title}" } ?: "展开后试着搜索“开发”或“designer”，也可以输入不存在的角色。")
}

@Composable
private fun DialogExample() {
    var visible by rememberSaveable { mutableStateOf(false) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
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
    var visible by rememberSaveable { mutableStateOf(false) }
    var updates by rememberSaveable { mutableStateOf(true) }
    var news by rememberSaveable { mutableStateOf(false) }
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
private fun SwitchExample() {
    var updates by rememberSaveable { mutableStateOf(false) }
    var changes by rememberSaveable { mutableIntStateOf(0) }
    SSwitch(updates, { updates = it; changes++ }, "工作空间更新",
        "开启后接收成员邀请、协作进展与重要变更。点击文字或右侧开关都能切换。", testTag = "switch_updates")
    SSwitch(true, { changes++ }, "系统通知", "此设置由管理员开启，当前不可更改。", enabled = false, testTag = "switch_disabled")
    ExampleNote("已切换 $changes 次", Modifier.testTag("switch_count"))
}

@Composable
private fun TextareaExample() {
    var notes by rememberSaveable { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    STextarea(notes, { notes = it }, "项目说明", placeholder = "写下目标、背景或下一步计划…",
        error = if (attempted && notes.isBlank()) "请填写项目说明。" else null, testTag = "textarea_notes")
    ExampleNote("支持换行、中文和文本选择。超过六行时，可在输入框内继续滚动。")
    STextarea("这段说明可以选择和复制。\n只读内容不会被编辑。", {}, "只读说明",
        minLines = 2, readOnly = true, testTag = "textarea_readonly")
    STextarea("当前不可编辑", {}, "禁用说明", minLines = 2, enabled = false, testTag = "textarea_disabled")
    SButton("验证说明", { attempted = true }, Modifier.fillMaxWidth().testTag("textarea_validate"), variant = ButtonVariant.Outline)
}

@Composable
private fun RadioGroupExample() {
    var selected by rememberSaveable { mutableStateOf<String?>("free") }
    var changes by rememberSaveable { mutableIntStateOf(0) }
    val options = remember {
        listOf(
            RadioOption("free", "免费版", "适合个人项目与初次体验。"),
            RadioOption("pro", "专业版", "适合需要共同编辑、管理成员和持续协作的团队。"),
            RadioOption("disabled", "企业版", "当前演示暂不提供此方案。", enabled = false),
        )
    }
    SRadioGroup(options, selected, { next ->
        if (selected != next) { selected = next; changes++ }
    }, "选择方案", testTag = "radio_plan")
    ExampleNote("当前方案：${options.firstOrNull { it.id == selected }?.label ?: "未选择"}", Modifier.testTag("radio_selected"))
    ExampleNote("已切换 $changes 次", Modifier.testTag("radio_count"))
}

@Composable
private fun AlertExample() {
    var connected by rememberSaveable { mutableStateOf(false) }
    SAlert("本地演示", "这里的更改只保存在当前设备，不会提交到服务器。", Modifier.testTag("alert_default"))
    SAlert(
        if (connected) "已重新连接" else "无法保存更改",
        if (connected) "现在可以继续编辑。" else "连接已断开。检查网络后再试一次。",
        Modifier.testTag("alert_destructive"),
        variant = if (connected) AlertVariant.Default else AlertVariant.Destructive,
        announce = true,
    )
    SButton(if (connected) "连接已恢复" else "重试保存", { connected = true },
        Modifier.fillMaxWidth().testTag("alert_trigger"), variant = ButtonVariant.Outline, enabled = !connected)
}

@Composable
private fun ExampleNote(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.bodySmall, color = ShadcnTheme.colors.mutedForeground)
}

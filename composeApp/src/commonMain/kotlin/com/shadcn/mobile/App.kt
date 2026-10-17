package com.shadcn.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadcn.mobile.form.*
import com.shadcn.mobile.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val FormSaver = listSaver<FormValues, Any>(
    save = { listOf(it.name, it.email, it.roleId, it.acceptedTerms, it.emailUpdates, it.productNews) },
    restore = { FormValues(it[0] as String, it[1] as String, it[2] as String, it[3] as Boolean, it[4] as Boolean, it[5] as Boolean) },
)

@Composable
fun App(initialDarkTheme: Boolean? = null, onThemeChanged: (Boolean) -> Unit = {}) {
    val systemDark = isSystemInDarkTheme()
    var darkOverride by rememberSaveable { mutableStateOf(initialDarkTheme) }
    val dark = darkOverride ?: systemDark
    SideEffect { onThemeChanged(dark) }
    ShadcnTheme(darkTheme = dark) {
        WorkspaceScreen(dark = dark, onToggleTheme = { darkOverride = !dark })
    }
}

@Composable
private fun WorkspaceScreen(dark: Boolean, onToggleTheme: () -> Unit) {
    var values by rememberSaveable(stateSaver = FormSaver) { mutableStateOf(FormValues()) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var reviewOpen by rememberSaveable { mutableStateOf(false) }
    var preferencesOpen by rememberSaveable { mutableStateOf(false) }
    var termsOpen by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    val errors = if (attempted) validateForm(values) else FormErrors()
    val colors = ShadcnTheme.colors
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val nameFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    fun review() {
        attempted = true
        if (validateForm(values).isValid) {
            values = values.normalized()
            focus.clearFocus()
            keyboard?.hide()
            reviewOpen = true
        } else {
            scope.launch { scroll.animateScrollTo(0) }
        }
    }

    Column(Modifier.fillMaxSize().background(colors.background).safeDrawingPadding().imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(28.dp).background(colors.primary, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides colors.onPrimary) {
                    SIcon("mark", size = 17.dp)
                }
            }
            Spacer(Modifier.width(10.dp))
            Text("shadcn-mobile", modifier = Modifier.weight(1f), fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold, color = colors.foreground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(
                Modifier.size(48.dp).testTag("theme_toggle")
                    .semantics { contentDescription = if (dark) "切换到浅色主题" else "切换到深色主题" }
                    .clickable(role = Role.Button, onClick = onToggleTheme),
                contentAlignment = Alignment.Center,
            ) { SIcon(if (dark) "sun" else "moon") }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Column(
            Modifier.fillMaxSize().testTag("form_scroll").verticalScroll(scroll).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 520.dp).fillMaxWidth().padding(top = 30.dp, bottom = 28.dp)) {
                if (submitted) {
                    SuccessContent(values, onRestart = {
                        values = FormValues()
                        attempted = false
                        submitted = false
                        scope.launch { scroll.scrollTo(0) }
                    })
                } else {
                    Text("创建你的工作空间", Modifier.semantics { heading() }, color = colors.foreground,
                        fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.7).sp)
                    Spacer(Modifier.height(8.dp))
                    Text("让想法有个开始的地方。\n填写基本信息，按你的方式开始。", color = colors.mutedForeground,
                        fontSize = 14.sp, lineHeight = 22.sp)
                    Spacer(Modifier.height(28.dp))
                    SInput(
                        value = values.name, onValueChange = { values = values.copy(name = it) },
                        label = "工作空间名称", placeholder = "例如：山间设计工作室", error = errors.name,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
                        focusRequester = nameFocus, testTag = "workspace_name",
                    )
                    Spacer(Modifier.height(20.dp))
                    SInput(
                        value = values.email, onValueChange = { values = values.copy(email = it) },
                        label = "工作邮箱", placeholder = "you@example.com", error = errors.email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); keyboard?.hide() }),
                        focusRequester = emailFocus, testTag = "email",
                    )
                    Spacer(Modifier.height(20.dp))
                    SCombobox(selectedId = values.roleId, onSelect = { values = values.copy(roleId = it) }, error = errors.role)
                    Spacer(Modifier.height(24.dp))
                    Row(
                        Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .testTag("preferences_button").clickable(role = Role.Button) {
                                focus.clearFocus(); keyboard?.hide(); preferencesOpen = true
                            }.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SIcon("sliders", size = 20.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("通知偏好", color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(3.dp))
                            Text(preferencesDescription(values), color = colors.mutedForeground, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                        SIcon("chevron-right", size = 17.dp)
                    }
                    Spacer(Modifier.height(18.dp))
                    SCheckbox(
                        checked = values.acceptedTerms, onCheckedChange = { values = values.copy(acceptedTerms = it) },
                        label = "我已阅读并同意演示条款", description = "表单仅用于组件体验，信息不会上传。",
                        error = errors.terms, testTag = "terms_checkbox",
                    )
                    SButton("查看演示条款", onClick = { termsOpen = true }, variant = ButtonVariant.Ghost,
                        modifier = Modifier.align(Alignment.Start).testTag("terms_button"))
                    Spacer(Modifier.height(18.dp))
                    SButton("创建工作空间", onClick = ::review, modifier = Modifier.fillMaxWidth().testTag("submit_button"),
                        trailingIcon = { SIcon("arrow-right", size = 18.dp) })
                    Spacer(Modifier.height(18.dp))
                    Text("本地交互原型 · 无需账户或网络", color = colors.mutedForeground, fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }

    if (preferencesOpen) {
        PreferencesSheet(
            initialEmailUpdates = values.emailUpdates, initialProductNews = values.productNews,
            onDismiss = { preferencesOpen = false },
            onSave = { email, news -> values = values.copy(emailUpdates = email, productNews = news); preferencesOpen = false },
        )
    }
    if (reviewOpen) {
        SDialog(title = "确认创建工作空间", description = "检查一下信息，一切就绪后即可开始。",
            onDismiss = { if (!submitting) reviewOpen = false }, tag = "review_dialog") {
            SummaryRows(values)
            Spacer(Modifier.height(24.dp))
            SButton(if (submitting) "正在创建…" else "确认创建", modifier = Modifier.fillMaxWidth().testTag("review_confirm"),
                enabled = !submitting, loading = submitting, onClick = {
                    submitting = true
                    scope.launch {
                        delay(700)
                        submitting = false
                        reviewOpen = false
                        submitted = true
                        scroll.scrollTo(0)
                    }
                })
            Spacer(Modifier.height(8.dp))
            SButton("返回修改", modifier = Modifier.fillMaxWidth().testTag("review_cancel"), variant = ButtonVariant.Outline,
                enabled = !submitting, onClick = { reviewOpen = false })
        }
    }
    if (termsOpen) {
        SDialog(title = "演示条款", description = "这是一个可交互的组件原型。", onDismiss = { termsOpen = false }, tag = "terms_dialog") {
            Text("你填写的信息只保留在当前演示状态中，不会发送到服务器，也不会创建真实账户。关闭演示后无需处理任何订阅或费用。",
                color = colors.mutedForeground, fontSize = 14.sp, lineHeight = 23.sp)
            Spacer(Modifier.height(24.dp))
            SButton("我知道了", { termsOpen = false }, Modifier.fillMaxWidth())
        }
    }
}

private fun preferencesDescription(values: FormValues): String = when {
    values.emailUpdates && values.productNews -> "工作空间更新与产品动态"
    values.emailUpdates -> "仅接收工作空间更新"
    values.productNews -> "仅接收产品动态"
    else -> "不接收邮件通知"
}

@Composable
internal fun SummaryRows(values: FormValues) {
    val colors = ShadcnTheme.colors
    Column(Modifier.fillMaxWidth().background(colors.muted, RoundedCornerShape(8.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SummaryRow("工作空间", values.name)
        SummaryRow("工作邮箱", values.email)
        SummaryRow("你的角色", Roles.firstOrNull { it.id == values.roleId }?.title.orEmpty())
        SummaryRow("通知偏好", preferencesDescription(values))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = ShadcnTheme.colors.mutedForeground)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = ShadcnTheme.colors.foreground)
    }
}

@Composable
private fun SuccessContent(values: FormValues, onRestart: () -> Unit) {
    val colors = ShadcnTheme.colors
    Column(Modifier.fillMaxWidth().testTag("success_panel")) {
        Box(Modifier.size(52.dp).background(colors.primary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides colors.onPrimary) { SIcon("check", size = 26.dp) }
        }
        Spacer(Modifier.height(24.dp))
        Text("一切就绪。", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.foreground,
            modifier = Modifier.semantics { heading(); liveRegion = LiveRegionMode.Polite })
        Spacer(Modifier.height(10.dp))
        Text("你的工作空间已在本次演示中创建。\n从这里，开始下一个好想法。", color = colors.mutedForeground, fontSize = 14.sp, lineHeight = 23.sp)
        Spacer(Modifier.height(28.dp))
        SummaryRows(values)
        Spacer(Modifier.height(28.dp))
        SButton("再创建一个", onRestart, Modifier.fillMaxWidth().testTag("create_another"), ButtonVariant.Outline)
        Spacer(Modifier.height(16.dp))
        Text("演示已完成，未创建在线账户。", color = colors.mutedForeground, fontSize = 12.sp)
    }
}

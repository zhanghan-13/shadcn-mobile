export const REPOSITORY = 'https://github.com/zhanghan-13/shadcn-mobile';

export const components = {
  button: {
    title: 'Button', zh: '按钮', name: 'SButton', source: 'ui/Primitives.kt',
    description: '采用黑白配色，提供主要、描边和轻量三种样式，用于区分操作层级。内置加载与禁用状态。',
    summary: '操作、加载与禁用状态',
    behavior: ['Primary 用于主要操作，Outline 用于次要操作，Ghost 用于轻量操作。', '触控区域至少 48dp；聚焦与按下时提供可见反馈。', 'loading 为 true 时显示进度并阻止再次点击。'],
    sample: `@Composable
fun ButtonExample() {
    var saved by remember { mutableStateOf(false) }

    SButton(
        text = if (saved) "已保存" else "保存修改",
        onClick = { saved = !saved },
        variant = ButtonVariant.Primary,
        trailingIcon = { SIcon("arrow-right") },
    )
}`,
    signature: `@Composable
fun SButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
)`,
    props: [['text', 'String', '按钮的可见文字。'], ['onClick', '() -> Unit', '由调用方处理操作。'], ['variant', 'ButtonVariant.Primary', 'Primary / Outline / Ghost。'], ['enabled', 'true', 'false 时降低强调并禁止交互。'], ['loading', 'false', '显示进度，防止重复点击。'], ['trailingIcon', 'null', '文字后方的 Compose 内容插槽。']],
  },
  input: {
    title: 'Input', zh: '输入框', name: 'SInput', source: 'ui/Primitives.kt',
    description: '带可见标签的文本输入框，外观简洁，可通过 Compose API 配置焦点、键盘操作与错误信息。',
    summary: '标签、文本输入与错误反馈',
    behavior: ['标签与错误信息都进入辅助功能语义。占位文字不会代替标签。', '使用 keyboardOptions 配置键盘类型，使用 keyboardActions 处理 Next / Done。', '校验由业务侧完成；组件只显示传入的 error。支持字体缩放与多行内容。'],
    sample: `@Composable
fun InputExample() {
    var email by rememberSaveable { mutableStateOf("") }

    SInput(
        value = email,
        onValueChange = { email = it },
        label = "工作邮箱",
        placeholder = "you@example.com",
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        ),
    )
}`,
    signature: `@Composable
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
)`,
    props: [['value / onValueChange', 'String / (String) -> Unit', '状态由调用方持有。'], ['label', 'String', '可见标签与可访问名称。'], ['placeholder', '""', '空值时的提示。'], ['error', 'null', '错误描边和帮助文字。'], ['singleLine', 'true', '设为 false 使用多行输入。'], ['keyboardOptions / keyboardActions', 'Default', '键盘类型与 IME 行为。'], ['focusRequester', 'null', '在字段之间转移焦点。']],
  },
  checkbox: {
    title: 'Checkbox', zh: '复选框', name: 'SCheckbox', source: 'ui/Primitives.kt',
    description: '支持整行点击的选择项。标签、说明和错误信息组成同一个可访问控件。',
    summary: '整行点选与说明文案',
    behavior: ['整行可以切换选中状态，最小触控高度为 48dp。', '标签和说明合并为一个 Checkbox 语义节点。', '支持禁用状态与错误说明；文字会随系统字号自然换行。'],
    sample: `@Composable
fun CheckboxExample() {
    var checked by rememberSaveable { mutableStateOf(false) }

    SCheckbox(
        checked = checked,
        onCheckedChange = { checked = it },
        label = "接收产品动态",
        description = "新功能与实用技巧，偶尔来信。",
    )
}`,
    signature: `@Composable
fun SCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    description: String? = null,
    error: String? = null,
    modifier: Modifier = Modifier,
    testTag: String = "",
    enabled: Boolean = true,
)`,
    props: [['checked / onCheckedChange', 'Boolean / (Boolean) -> Unit', '控制选中状态。'], ['label', 'String', '主要标签，支持自然换行。'], ['description', 'null', '标签下方的补充说明。'], ['error', 'null', '校验失败时的说明与描边。'], ['enabled', 'true', '是否允许切换。']],
  },
  combobox: {
    title: 'Combobox', zh: '组合框', name: 'SCombobox', source: 'ui/Overlays.kt',
    description: '先搜索，再选择。角色选择器支持中英文搜索和无结果恢复，可用于体验移动表单中的选择流程。',
    summary: '搜索、选择与无结果恢复',
    behavior: ['支持搜索中文名称、英文描述和角色 ID。Developer 或 开发者 都能找到同一项。', '搜索选择弹窗为系统键盘留出空间，列表可以滚动。选择后关闭弹窗；再次打开保留选择并清空搜索。', '关闭时尝试将焦点还给触发器。Android 已验证系统返回关闭。'],
    note: '当前 SCombobox 使用演示中的 Roles 数据：designer、developer、product、other。通用 options 参数尚未提供。',
    sample: `@Composable
fun ComboboxExample() {
    var roleId by rememberSaveable { mutableStateOf("") }

    SCombobox(
        selectedId = roleId,
        onSelect = { roleId = it },
        error = null,
    )
}`,
    signature: `@Composable
fun SCombobox(
    selectedId: String,
    onSelect: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier,
)`,
    props: [['selectedId', 'String', '选中角色的 ID；空字符串表示未选择。'], ['onSelect', '(String) -> Unit', '返回所选角色 ID。'], ['error', 'null', '控件错误语义及说明。'], ['modifier', 'Modifier', '控制宽度和外部布局。']],
  },
  dialog: {
    title: 'Dialog', zh: '对话框', name: 'SDialog', source: 'ui/Overlays.kt',
    description: '用于确认操作的模态窗口，由标题、说明和内容插槽组成，内容可以滚动。具体操作由调用方决定。',
    summary: '信息确认与可滚动内容',
    behavior: ['由外部 Boolean 状态控制是否显示；在 onDismiss 中关闭。', '内容区域可以滚动，字体放大时操作仍可到达。', '标题提供 heading 与 paneTitle 语义。Android 已验证返回关闭与草稿保留。'],
    sample: `@Composable
fun DialogExample() {
    var open by remember { mutableStateOf(false) }

    SButton("查看确认弹窗", onClick = { open = true })
    if (open) {
        SDialog(
            title = "一切准备就绪？",
            description = "确认后开始你的工作空间。",
            onDismiss = { open = false },
        ) {
            SButton(
                text = "确认",
                onClick = { open = false },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}`,
    signature: `@Composable
fun SDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    tag: String = "dialog",
    content: @Composable ColumnScope.() -> Unit,
)`,
    props: [['title', 'String', '窗口标题与辅助功能窗口名称。'], ['description', 'String', '标题下方的说明。'], ['onDismiss', '() -> Unit', '响应窗口关闭请求。'], ['content', 'ColumnScope.() -> Unit', '放入摘要、内容和操作按钮。'], ['tag', '"dialog"', '用于 Compose UI 测试。']],
  },
  sheet: {
    title: 'Sheet', zh: '底部面板', name: 'PreferencesSheet', source: 'ui/Overlays.kt',
    description: '在当前任务中，通过底部面板设置通知偏好。修改会暂存在面板中，点击保存后才应用到表单。',
    summary: '暂存设置、保存与取消',
    behavior: ['初次打开时从 initialEmailUpdates 与 initialProductNews 初始化。', '保存后调用 onSave；取消、返回或拖动关闭时丢弃未保存修改。', '基于 ModalBottomSheet，保留平台弹层与拖动行为，内容支持滚动。'],
    note: '当前实现名称是 PreferencesSheet，内容为通知偏好。它是可运行的业务示例，尚未抽象成通用的 SSheet。',
    sample: `@Composable
fun SheetExample() {
    var open by remember { mutableStateOf(false) }
    var updates by rememberSaveable { mutableStateOf(true) }
    var news by rememberSaveable { mutableStateOf(false) }

    SButton("通知偏好", onClick = { open = true })
    if (open) {
        PreferencesSheet(
            initialEmailUpdates = updates,
            initialProductNews = news,
            onDismiss = { open = false },
            onSave = { nextUpdates, nextNews ->
                updates = nextUpdates
                news = nextNews
                open = false
            },
        )
    }
}`,
    signature: `@Composable
fun PreferencesSheet(
    initialEmailUpdates: Boolean,
    initialProductNews: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean) -> Unit,
)`,
    props: [['initialEmailUpdates', 'Boolean', '工作空间更新的初始状态。'], ['initialProductNews', 'Boolean', '产品动态的初始状态。'], ['onDismiss', '() -> Unit', '关闭面板，不应用待保存修改。'], ['onSave', '(Boolean, Boolean) -> Unit', '依次返回工作空间更新、产品动态的选择。']],
  },
  switch: {
    title: 'Switch', zh: '开关', name: 'SSwitch', source: 'ui/FormComponents.kt',
    description: '用开关表示立即生效的设置。标签、说明与开关组成一个可操作的整行，状态和保存方式由调用方决定。',
    summary: '开关状态、整行操作与禁用',
    behavior: ['checked 由调用方持有，点击整行后通过 onCheckedChange 返回新状态。', '交互区域至少 48dp，整行合并为一个 Switch 语义节点。', 'enabled 为 false 时保留当前状态并阻止切换；说明文字可以换行。'],
    sample: `@Composable
fun SwitchExample() {
    var enabled by rememberSaveable { mutableStateOf(false) }

    SSwitch(
        checked = enabled,
        onCheckedChange = { enabled = it },
        label = "接收工作空间更新",
        description = "成员邀请与协作进展发生时通知我。",
    )
}`,
    signature: `@Composable
fun SSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "",
)`,
    props: [['checked / onCheckedChange', 'Boolean / (Boolean) -> Unit', '控制开关状态，响应整行点击。'], ['label', 'String', '可见标签与可访问名称。'], ['description', 'null', '标签下方的补充说明。'], ['modifier', 'Modifier', '控制外部布局。'], ['enabled', 'true', 'false 时禁止切换。'], ['testTag', '""', '用于定位 Compose UI 测试节点。']],
  },
  textarea: {
    title: 'Textarea', zh: '多行输入框', name: 'STextarea', source: 'ui/FormComponents.kt',
    description: '用于介绍、备注等多行文本。保留可见标签、焦点描边和错误信息，支持只读与禁用状态。',
    summary: '多行文本、只读与错误反馈',
    behavior: ['Enter 可以输入换行；minLines 与 maxLines 控制可见行数，须满足 1 ≤ minLines ≤ maxLines。文本状态由调用方持有。', 'readOnly 保留文本选择和复制，阻止编辑；enabled 为 false 时禁用输入。', '标签和错误信息进入辅助功能语义。键盘类型与动作由 keyboardOptions、keyboardActions 配置。'],
    sample: `@Composable
fun TextareaExample() {
    var introduction by rememberSaveable { mutableStateOf("") }

    STextarea(
        value = introduction,
        onValueChange = { introduction = it },
        label = "工作空间介绍",
        placeholder = "介绍团队正在做的事，可以分行填写。",
        minLines = 3,
        maxLines = 6,
    )
}`,
    signature: `@Composable
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
)`,
    props: [['value / onValueChange', 'String / (String) -> Unit', '多行文本由调用方持有。'], ['label', 'String', '可见标签与可访问名称。'], ['modifier', 'Modifier', '控制宽度和外部布局。'], ['placeholder', '""', '空值时的提示，不替代标签。'], ['error', 'null', '错误描边与说明文字。'], ['minLines / maxLines', '3 / 6', '可见行数范围。'], ['enabled', 'true', '是否允许交互。'], ['readOnly', 'false', '保留选择与复制，阻止编辑。'], ['keyboardOptions / keyboardActions', 'Default', '系统键盘类型与 IME 动作。'], ['focusRequester', 'null', '由外部请求焦点。'], ['testTag', '""', '用于定位 Compose UI 测试节点。']],
  },
  'radio-group': {
    title: 'RadioGroup', zh: '单选组', name: 'SRadioGroup', source: 'ui/FormComponents.kt',
    description: '从一组选项中选择一项。每行可以带说明，支持禁用单个选项或整个组，适合方案与偏好选择。',
    summary: '互斥选择、选项说明与禁用',
    behavior: ['selectedId 为 null 时不选中任何项；选择后通过 onSelect 返回选项 ID。', '整行均可选择，选项提供 RadioButton 语义，组使用 selectableGroup。', 'RadioOption.enabled 控制单项，组件 enabled 控制整个组；两者均启用时才能选择。'],
    sample: `@Composable
fun RadioGroupExample() {
    var plan by rememberSaveable { mutableStateOf<String?>("free") }
    val plans = listOf(
        RadioOption("free", "个人", "适合个人项目与灵感记录。"),
        RadioOption("pro", "团队", "与团队一起整理和推进工作。"),
        RadioOption("enterprise", "企业", "暂未开放。", enabled = false),
    )

    SRadioGroup(
        options = plans,
        selectedId = plan,
        onSelect = { plan = it },
        label = "选择方案",
    )
}`,
    signature: `@Composable
fun SRadioGroup(
    options: List<RadioOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    testTag: String = "",
)`,
    props: [['options', 'List<RadioOption>', '选项的 ID、标签、说明与启用状态。'], ['selectedId', 'String?', '已选选项的 ID；null 表示未选择。'], ['onSelect', '(String) -> Unit', '返回用户选择的选项 ID。'], ['label', 'String', '组的可见标签。'], ['modifier', 'Modifier', '控制外部布局。'], ['error', 'null', '显示组级错误说明。'], ['enabled', 'true', 'false 时禁用所有选项。'], ['testTag', '""', '用于定位 Compose UI 测试节点。']],
    types: [{ name: 'RadioOption', description: '同一组内的 ID 必须唯一；enabled 可以单独禁用某一项。', signature: `data class RadioOption(
    val id: String,
    val label: String,
    val description: String? = null,
    val enabled: Boolean = true,
)` }],
  },
  alert: {
    title: 'Alert', zh: '提示信息', name: 'SAlert', source: 'ui/FormComponents.kt',
    description: '在页面中展示提示或警告。标题和说明一起呈现，可选择普通或危险样式，并按需请求辅助功能播报。',
    summary: '页面提示、危险样式与动态播报',
    behavior: ['Default 用于一般说明，Destructive 用于错误或需要注意的情况。', '内容直接显示在页面内，不打开弹窗、不阻断当前任务。', 'announce 默认为 false；开启后使用 live region 语义。实际播报行为仍需用目标设备的读屏工具验证。'],
    note: '预览中的“重试保存”只切换本地提示状态，不会发送网络请求。',
    sample: `@Composable
fun AlertExample() {
    SAlert(
        title = "暂时无法保存",
        description = "请检查网络连接后重试。当前输入仍会保留。",
        variant = AlertVariant.Destructive,
        announce = true,
    )
}`,
    signature: `@Composable
fun SAlert(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    variant: AlertVariant = AlertVariant.Default,
    announce: Boolean = false,
)`,
    props: [['title', 'String', '提示标题。'], ['description', 'null', '标题下方的补充说明。'], ['modifier', 'Modifier', '控制宽度和外部布局。'], ['variant', 'AlertVariant.Default', 'Default / Destructive。'], ['announce', 'false', '为动态内容启用 live region 语义。']],
    types: [{ name: 'AlertVariant', description: '选择普通提示或危险提示的文字与描边颜色。', signature: `enum class AlertVariant { Default, Destructive }` }],
  },
};

export const pages = [
  { route: 'overview', title: '介绍与体验', category: '开始使用', description: '体验完整表单与十个组件示例', keywords: '介绍 入门 完整表单 在线体验 demo form overview' },
  { route: 'installation', title: '快速开始', category: '开始使用', description: '在 Android 与 iOS 运行项目', keywords: '安装 运行 Android iOS Gradle Xcode Kotlin 下载 clone installation' },
  { route: 'theming', title: '主题与令牌', category: '开始使用', description: 'Zinc 配色、深浅主题与自定义', keywords: '主题 深色 浅色 配色 color theme token zinc' },
  ...Object.entries(components).map(([key, entry]) => ({ route: `components/${key}`, title: entry.title, zh: entry.zh, category: '组件', description: entry.summary, keywords: `${key} ${entry.zh} ${entry.name} ${entry.description} ${(entry.types || []).map((type) => type.name).join(' ')}` })),
];

export const tokens = [
  ['background', '#FAFAFA', '#09090B', '页面背景'], ['foreground', '#18181B', '#FAFAFA', '主要文字与图标'],
  ['surface', '#FFFFFF', '#18181B', '输入与弹层表面'], ['muted', '#F4F4F5', '#27272A', '次级背景、选中反馈'],
  ['mutedForeground', '#71717A', '#A1A1AA', '说明与占位文字'], ['border', '#E4E4E7', '#3F3F46', '分隔线与边框'],
  ['input', '#D4D4D8', '#52525B', '输入框边框'], ['primary', '#18181B', '#FAFAFA', '主要操作背景'],
  ['onPrimary', '#FAFAFA', '#18181B', '主要操作文字'], ['error', '#B91C1C', '#FCA5A5', '错误文字与边框'],
];

export function searchPages(query) {
  const terms = query.trim().toLocaleLowerCase().split(/\s+/).filter(Boolean);
  return pages.filter((page) => terms.every((term) => `${page.title} ${page.zh || ''} ${page.description} ${page.keywords}`.toLocaleLowerCase().includes(term)));
}

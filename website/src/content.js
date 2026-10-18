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
};

export const pages = [
  { route: 'overview', title: '介绍与体验', category: '开始使用', description: '体验六个组件串联的完整表单', keywords: '介绍 入门 完整表单 在线体验 demo form overview' },
  { route: 'installation', title: '快速开始', category: '开始使用', description: '在 Android 与 iOS 运行项目', keywords: '安装 运行 Android iOS Gradle Xcode Kotlin 下载 clone installation' },
  { route: 'theming', title: '主题与令牌', category: '开始使用', description: 'Zinc 配色、深浅主题与自定义', keywords: '主题 深色 浅色 配色 color theme token zinc' },
  ...Object.entries(components).map(([key, entry]) => ({ route: `components/${key}`, title: entry.title, zh: entry.zh, category: '组件', description: entry.summary, keywords: `${key} ${entry.zh} ${entry.name} ${entry.description}` })),
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

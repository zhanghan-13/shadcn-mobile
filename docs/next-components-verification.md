# Tabs、Accordion、Slider、Progress 验证记录

更新于 **2026-10-18**。本批将共享组件从 10 个扩展到 14 个，Android、iOS 与文档站使用同一份 Kotlin 示例。Android 本地完整回归已通过；**扩展后的 17 项 iOS 测试尚未执行**，不能用上批 13 项测试的结果代替本批验证。

## 当前结果

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| Android 应用、测试 APK，两个 iOS KLIB 目标和 Wasm 构建 | 成功，122 个任务，耗时 1 分 22 秒 | [构建日志](next-components-build-run.txt) |
| 共享逻辑测试 | 10 项通过，无失败、错误或跳过 | `composeApp/build/test-results/testAndroidHostTest/` |
| Android 完整交互回归 | 21 项通过，耗时 40.819 秒 | [完整运行日志](next-components-android-run.txt) |
| 本批 4 个组件，深色、大字号、窄屏 | 4 项通过，耗时 7.475 秒 | [运行日志](next-components-android-dark-large-run.txt) |
| 文档源码校验 | 17 个路由、14 个 Kotlin API、4 个关联类型通过；搜索、预览注册、主题令牌和相对资源路径匹配 | `node website/verify.mjs` |
| iOS 交互测试 | 已增加到 17 项，等待 macOS runner 执行 | [XCUITest 源码](../iosApp/iosAppUITests/WorkspaceFlowTests.swift) |
| 本批浏览器交互 | 检查中，暂未形成最终记录 | 完成后补充 |

iOS KLIB 编译只检查共享 Kotlin 代码；它不等同于 Apple framework 链接、Swift 宿主编译或 iOS 模拟器测试通过。

## Android 覆盖范围

完整回归保留之前的表单、弹层、键盘、主题、目录导航、状态恢复和禁用回调测试，并新增以下检查：

- **Tabs：** 初始选中、面板切换、中文草稿保留、Done 收起键盘；禁用标签在触摸和语义动作下均不切换。独立测试还验证 RTL 方向键、Home/End、跳过禁用项，以及面板内部 `rememberSaveable` 状态保留。
- **Accordion：** 默认收起、一次展开一项、再次点击收起；关闭的内容从语义树移除；禁用标题不会改变展开项或触发回调。
- **Slider：** 原生手势拖动、10 点步进、调整完成回调、辅助功能 `SetProgress` 与禁用保护，交互高度至少 48dp。负范围 `-10..0` 配合一个中间刻度时，`SetProgress(-3)` 返回 `-5`，越界请求限制为 `0`；禁用后数值和两类回调计数保持不变。
- **Progress：** 25% 起步，依次推进到 100%，完成后禁用推进按钮，再重置到 0%；数值与范围语义一致，未知进度为 `Indeterminate` 并显示“进度未定”。进度条不提供点击或调整动作。

深色回归使用 **150% 系统字号、460 dpi，约 375dp 的可用宽度**，只重跑本批四个组件的交互测试。这不代表所有 21 项都经过该显示配置验证。

## 测试中修复的问题

第一次运行包含 20 项测试，其中 2 项失败。Slider 自定义拇指将原生交互边界缩到 20dp，已把拇指布局槽改为 48dp 高，同时保留 20dp 的可见圆点。RTL 键盘测试运行在 Android 触摸输入模式，无法取得按钮键盘焦点；测试现在先切换到 `InputMode.Keyboard`，再验证真实方向键行为。

第二次扩展为 21 项测试，仅 Slider 禁用初值的严格比较失败：底层范围语义返回 `60.000004`。现在先用 `0.001` 容差确认初值约为 60，再要求禁用操作前后的完整范围语义严格相等，同时检查数值与完成回调计数均未改变。未降低禁用行为的验证要求。

Slider 还统一了输入、手势和辅助功能请求的范围限制与刻度吸附，补充负范围回归。第三次完整运行全部 21 项通过，随后本批四项深色、大字号测试也通过。

## 截图

下列八张截图来自本批 Android 运行，未替换上一批 iOS 报告或截图。

| 组件 | 常规显示 | 深色、150% 字号、460 dpi |
| --- | --- | --- |
| Tabs | [个人签名草稿](screenshots/android-tabs.png) | [大字号](screenshots/android-tabs-dark-large.png) |
| Accordion | [展开内容](screenshots/android-accordion.png) | [大字号](screenshots/android-accordion-dark-large.png) |
| Slider | [数值与禁用状态](screenshots/android-slider.png) | [大字号](screenshots/android-slider-dark-large.png) |
| Progress | [完成与未知进度](screenshots/android-progress.png) | [大字号](screenshots/android-progress-dark-large.png) |

## 尚未完成的验证

本批 iOS 17 项需要在独立验证分支的 GitHub macOS runner 上执行。计划检查 Tabs 草稿和禁用标签、Accordion 单项展开、Slider 原生拖动与禁用保护、Progress 完成与重置，并回归已有完整表单流程。执行后再记录实际环境、结果与耗时。

真机、手动 VoiceOver 和 iOS 大字号仍未验证。之前十个组件的记录保留在[原生验证](verification.md)、[CI 验证](ci-validation.md)和[浏览器验证](web-verification.md)中。

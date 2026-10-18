# Tabs、Accordion、Slider、Progress 验证记录

更新于 **2026-10-18**。本批将共享组件从 10 个扩展到 14 个，Android、iOS 与文档站使用同一份 Kotlin 示例。**10 项共享测试、21 项 Android 交互测试和 17 项 iOS 交互测试全部通过**；本地浏览器中的四个新组件交互也已验证。

## 当前结果

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| Android 应用、测试 APK，两个 iOS KLIB 目标和 Wasm 构建 | 成功，122 个任务，耗时 1 分 22 秒 | [构建日志](next-components-build-run.txt) |
| 共享逻辑测试 | 10 项通过，无失败、错误或跳过 | `composeApp/build/test-results/testAndroidHostTest/` |
| Android 完整交互回归 | 21 项通过，耗时 40.819 秒 | [完整运行日志](next-components-android-run.txt) |
| Android RTL 测试补强后的本地完整回归 | 21 项通过，耗时 34.17 秒；三个启用标签用于区分左右方向 | [运行日志](next-components-android-rtl-run.txt) |
| 系统返回注入修复后的本地完整回归 | 21 项通过，耗时 33.244 秒；仍检查弹层关闭、草稿与偏好保留 | [运行日志](next-components-android-native-back-run.txt) |
| 验证分支 Android CI | `94a3764` 通过 10 项共享测试与 21 项交互测试，141.126 秒，收集 14 张截图；RTL 用例尚为两个启用标签 | [运行 39131075167](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39131075167) |
| RTL 测试补强后的 Android CI | `2b6ce72` 的 10 项共享测试通过；交互测试 20 项通过、1 项失败，155.049 秒。新增 RTL 用例通过，旧弹层返回用例等待窗口焦点超时 | [运行 39131594487](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39131594487) |
| 系统返回注入修复后的 Android CI | `9d4b568` 通过 10 项共享测试与全部 21 项交互测试，0 失败 / 跳过 / 重试；98.478 秒，收集 14 张截图 | [运行 39132385060](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39132385060) |
| 本批 4 个组件，深色、大字号、窄屏 | 4 项通过，耗时 7.475 秒 | [运行日志](next-components-android-dark-large-run.txt) |
| 文档源码校验 | 17 个路由、14 个 Kotlin API、4 个关联类型通过；搜索、预览注册、主题令牌和相对资源路径匹配 | `node website/verify.mjs` |
| 验证分支 Docs CI | `94a3764` 的 Wasm 构建、17 路由 / 14 API / 4 类型与发布资源校验通过；上传 Pages 产物，分支未部署 | [运行 39131082059](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39131082059) |
| iOS 交互测试 | `94a3764` 通过全部 17 项，0 失败 / 跳过 / 重试，无测试进程重启；1139.028 秒 | [运行 39131068971](https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39131068971) |
| 本批浏览器交互 | 本地 Chrome 中的真实 Compose/Wasm 预览通过四个新组件的指针与文本交互 | [交互记录与截图](#本地浏览器交互) |

iOS 的通过结果来自 macOS runner 上的 Apple framework 链接、Swift 宿主编译与原生模拟器测试，包含完整中文表单流程，不以本地 KLIB 编译代替运行验证。

Docs CI 的构建任务运行于 13:20:10–13:22:05 UTC，耗时 1 分 55 秒；其中 Wasm 构建耗时 1 分 31 秒。Pages 产物 `12137572828` 已上传，部署任务按验证分支规则跳过。

首轮 Android CI 运行于 13:20:05–13:26:11 UTC，耗时 6 分 6 秒；APK、测试报告与 14 张截图保存在产物 `12138490411` 中。后续 RTL 测试补强的结果单独记录，不将这次通过归因于当时尚未包含的测试变化。

最终 Android CI `39132385060` 对应 `9d4b5686e3f64ce64da42fb4ceb8f53c5a8971ab`，运行于 13:37:52–13:42:51 UTC，耗时 4 分 59 秒。完整日志确认 21 个不同方法各开始并通过一次，没有失败、跳过或重试；14 张截图均已收集并通过非空校验。APK、报告和截图保存在产物 `12138990540` 中。生产代码与最初的 `94a3764` 一致，`2b6ce72` 和 `9d4b568` 只补强 Android 测试。

iOS CI `39131068971` 对应 `94a376443800426147a505b41c1210b073c97049`，整个任务耗时 36 分 43 秒，17 项交互测试耗时 1139.028 秒。后续变化仅涉及 Android 测试与文档，共享组件、原生宿主和 iOS 测试源码一致；iOS 结果仍归属于实际执行的 `94a3764`。

## iOS 覆盖范围

本次运行使用 Apple silicon `macos-26` runner，macOS 26.6.2、Xcode 26.4.1、Swift 6.3.1，以及 iPhone 17 Pro / iOS 26.5 模拟器。17 个不同测试各运行一次并全部通过，没有失败、跳过、重试或测试进程重启。完整中文表单流程通过，耗时 182.695 秒。

原始 [xcresult 摘要](next-components-ios-test-summary.json)与[逐项结果](next-components-ios-test-results.json)已独立归档，摘要记录 17 通过、0 失败、0 跳过、0 预期失败；逐项结果通过严格的 17 项检查。以下截图来自同一次运行的成功附件，上一批 iOS 报告和截图保持不变。

| 本批组件 | 原生交互检查 | 耗时 | 成功截图 |
| --- | --- | --- | --- |
| Tabs | 中文草稿、Done 收键盘、切换后保留草稿、禁用项不能选择 | 74.183 秒 | [草稿保留](screenshots/ios-tabs.png) |
| Accordion | 默认收起、替换展开项、再次收起、禁用标题不改变状态 | 90.393 秒 | [展开内容](screenshots/ios-accordion.png) |
| Slider | 真实拖动、10 点步进、完成回调一次；禁用拖动不改变值和回调计数 | 42.337 秒 | [滑块状态](screenshots/ios-slider.png) |
| Progress | 25% 推进到 100%、完成后禁用、重置为 0%、未知进度语义 | 70.911 秒 | [完成状态](screenshots/ios-progress.png) |

## Android 覆盖范围

完整回归保留之前的表单、弹层、键盘、主题、目录导航、状态恢复和禁用回调测试，并新增以下检查：

- **Tabs：** 初始选中、面板切换、中文草稿保留、Done 收起键盘；禁用标签在触摸和语义动作下均不切换。独立测试还验证 RTL 方向键、Home/End、跳过禁用项，以及面板内部 `rememberSaveable` 状态保留。
- **Accordion：** 默认收起、一次展开一项、再次点击收起；关闭的内容从语义树移除；禁用标题不会改变展开项或触发回调。
- **Slider：** 原生手势拖动、10 点步进、调整完成回调、辅助功能 `SetProgress` 与禁用保护，交互高度至少 48dp。负范围 `-10..0` 配合一个中间刻度时，`SetProgress(-3)` 返回 `-5`，越界请求限制为 `0`；禁用后数值和两类回调计数保持不变。
- **Progress：** 25% 起步，依次推进到 100%，完成后禁用推进按钮，再重置到 0%；数值与范围语义一致，未知进度为 `Indeterminate` 并显示“进度未定”。进度条不提供点击或调整动作。

深色回归使用 **150% 系统字号、460 dpi，约 375dp 的可用宽度**，只重跑本批四个组件的交互测试。四张截图已检查，未见文字重叠；Tabs 在该宽度下通过横向滚动容纳标签。这不代表所有 21 项都经过该显示配置验证。

## 测试中修复的问题

第一次运行包含 20 项测试，其中 2 项失败。Slider 自定义拇指将原生交互边界缩到 20dp，已把拇指布局槽改为 48dp 高，同时保留 20dp 的可见圆点。RTL 键盘测试运行在 Android 触摸输入模式，无法取得按钮键盘焦点；测试现在先切换到 `InputMode.Keyboard`，再验证真实方向键行为。

第二次扩展为 21 项测试，仅 Slider 禁用初值的严格比较失败：底层范围语义返回 `60.000004`。现在先用 `0.001` 容差确认初值约为 60，再要求禁用操作前后的完整范围语义严格相等，同时检查数值与完成回调计数均未改变。未降低禁用行为的验证要求。

Slider 还统一了输入、手势和辅助功能请求的范围限制与刻度吸附，补充负范围回归。第三次完整运行全部 21 项通过，随后本批四项深色、大字号测试也通过。

复核时发现，只有两个启用标签的 RTL 用例无法区分向左和向右的环绕结果。提交 `2b6ce72` 将测试改为三个启用标签，并在第一项和中间项之间放置一个禁用项，明确要求 RTL 左键从第一项到中间项、右键返回第一项，再验证 Home/End。生产代码未改动。补强后的本地完整回归 21 项通过，耗时 34.17 秒。

该提交的云端运行 `39131594487` 执行了全部 21 项，20 项通过、1 项失败。三个启用标签的 RTL 用例通过；失败的是原有 `systemBackClosesEachOverlayWithoutLosingDraft`，在 `WorkspaceFlowTest.kt:210` 调用 `Espresso.pressBack` 时抛出 `RootViewWithoutFocusException`。日志显示 Activity 根窗口 `has-window-focus=false`，等待焦点 10 秒超时。错误出现在按键注入前，尚未执行弹层关闭断言。

提交 `9d4b568` 统一替换测试中的 8 处 Espresso 返回操作：先等待 Compose 空闲，再用 `Instrumentation.sendKeyDownUpSync(KEYCODE_BACK)` 向当前聚焦窗口发送返回键，最后再次等待空闲。该 API 的事件目标和同步行为见 [Android 官方文档](https://developer.android.com/reference/android/app/Instrumentation#sendKeyDownUpSync(int))。此改动只修正测试的事件注入方式，不修改生产代码、不重试操作，也不放宽关闭、草稿或偏好断言。修复后的本地 21 项完整回归通过，耗时 33.244 秒；云端全部 21 项也通过，耗时 98.478 秒。

## 截图

下列八张截图来自本批 Android 运行，均已检查，未见文字重叠或截断；Tabs 的大字号布局可横向滚动。上一批 iOS 报告和截图保持不变。

| 组件 | 常规显示 | 深色、150% 字号、460 dpi |
| --- | --- | --- |
| Tabs | [个人签名草稿](screenshots/android-tabs.png) | [大字号](screenshots/android-tabs-dark-large.png) |
| Accordion | [展开内容](screenshots/android-accordion.png) | [大字号](screenshots/android-accordion-dark-large.png) |
| Slider | [数值与禁用状态](screenshots/android-slider.png) | [大字号](screenshots/android-slider-dark-large.png) |
| Progress | [完成与未知进度](screenshots/android-progress.png) | [大字号](screenshots/android-progress-dark-large.png) |

## 本地浏览器交互

以下检查在本地 Chrome 的真实 Compose/Wasm 预览中完成，没有用 HTML 重建组件。它们验证当前本地构建，不代表新版本已经发布到 GitHub Pages。

| 组件 | 实际操作与结果 | 截图 |
| --- | --- | --- |
| Tabs | 输入“保留这份中文草稿”，切换到活动、点击禁用标签，再回到资料；草稿保留，切换计数为 2 | [草稿保留](screenshots/web-tabs.png) |
| Accordion | 展开 native，再展开 ownership，前一内容被替换；点击禁用标题后计数仍为 2；再次点击 ownership 后收起，计数为 3 | [面板切换](screenshots/web-accordion.png) |
| Slider | 实际拖动从 40 到 80，按 10 步进，调整计数 4、完成计数 1；拖动禁用滑块不改变数值或计数；点击主轨道到 20，计数变为 5 和 2 | [滑块状态](screenshots/web-slider.png) |
| Progress | 从 25% 推进到 100%，完成按钮禁用，再次点击仍为 100%；重置后为 0%，未知进度条显示“进度未定” | [完成状态](screenshots/web-progress.png) |

重新构建并刷新后，Accordion 参数表中的 `_${id}` / `_${id}_content` 已正确显示。Progress 切换到浅色主题时，文档与预览同步更新，见[浅色截图](screenshots/web-progress-light.png)。

浏览器 Slider 在鼠标操作后直接按右方向键没有改变数值，尚未验证通过键盘取得焦点后的完整路径，因此不将 Web Slider 键盘交互记为通过。Android 独立测试中的 RTL 标签键盘路径已通过，两者是不同检查。

本次控制台未捕获新的异常，但每次预览加载仍出现依赖中的 `wasmExports` memory 弃用日志；该日志在[之前的浏览器验证](web-verification.md)中也有记录，不能将控制台描述为没有警告。

## 尚未完成的验证

真机、手动 VoiceOver 和 iOS 大字号仍未验证。之前十个组件的记录保留在[原生验证](verification.md)、[CI 验证](ci-validation.md)和[浏览器验证](web-verification.md)中。

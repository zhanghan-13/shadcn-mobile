import { REPOSITORY, components, pages, tokens, searchPages } from './content.js';

const $ = (selector, scope = document) => scope.querySelector(selector);
const $$ = (selector, scope = document) => [...scope.querySelectorAll(selector)];
const escapeHtml = (value) => String(value).replace(/[&<>"']/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[character]));
const paths = {
  arrow: '<path d="M5 12h14m-6-6 6 6-6 6"/>',
  chevron: '<path d="m9 6 6 6-6 6"/>',
  external: '<path d="M14 4h6v6m0-6L10 14"/><path d="M10 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-5"/>',
  copy: '<rect x="9" y="9" width="11" height="11" rx="2"/><path d="M15 9V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h4"/>',
  check: '<path d="m5 12 4 4L19 6"/>', close: '<path d="m6 6 12 12M18 6 6 18"/>',
  reload: '<path d="M20 7v5h-5"/><path d="M19 12a7 7 0 1 0-2 5M20 12a8 8 0 0 0-2-6"/>',
  phone: '<rect x="7" y="2" width="10" height="20" rx="2"/><path d="M11 18h2"/>',
  wide: '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M9 22h6M12 19v3"/>',
  code: '<path d="m8 7-5 5 5 5m8-10 5 5-5 5m-3-14-2 18"/>',
  moon: '<path d="M20.5 13a8.5 8.5 0 0 1-9-9.5 8.5 8.5 0 1 0 9 9.5Z"/>',
  sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2m0 16v2M2 12h2m16 0h2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
  layers: '<path d="m12 3 10 5-10 5L2 8l10-5Zm-10 9 10 5 10-5M2 16l10 5 10-5"/>',
};
const icon = (name) => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${paths[name] || paths.code}</svg>`;
const codeValues = new Map();
history.scrollRestoration = 'manual';
let currentRoute = '';
let toastTimer;
let searchIndex = 0;

function highlight(code, language) {
  if (language !== 'kotlin') return escapeHtml(code);
  return code.split(/(\/\/[^\n]*|"(?:[^"\\]|\\.)*"|@[A-Za-z]+|\b(?:fun|var|val|if|else|true|false|null|by|import|return|class|data|object|private)\b|\b(?:SButton|SInput|SCheckbox|SCombobox|SDialog|PreferencesSheet|ShadcnTheme|Modifier|ButtonVariant|Color|Boolean|String|Unit|KeyboardOptions|KeyboardType|ImeAction|ColumnScope|FocusRequester)\b)/g)
    .map((token) => {
      const type = token.startsWith('//') ? 'comment' : token.startsWith('"') ? 'string' : /^@|^(fun|var|val|if|else|true|false|null|by|import|return|class|data|object|private)$/.test(token) ? 'keyword' : /^[A-Z][\w]*$/.test(token) ? 'type' : '';
      return type ? `<span class="code-${type}">${escapeHtml(token)}</span>` : escapeHtml(token);
    }).join('');
}
function codeBlock(code, label = 'Kotlin', language = 'kotlin') {
  const id = `code-${codeValues.size}`;
  codeValues.set(id, code);
  return `<div class="code-block"><div class="code-header"><span>${escapeHtml(label)}</span><button class="copy-button" data-copy="${id}" aria-label="复制${escapeHtml(label)}代码">${icon('copy')}<span>复制</span></button></div><pre tabindex="0"><code>${highlight(code, language)}</code></pre></div>`;
}
const buttonLink = (route, label, primary = false) => `<a class="text-button${primary ? ' primary' : ''}" href="#/${route}">${label}${icon('arrow')}</a>`;
const externalLink = (url, label, className = '') => `<a class="${className}" href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer">${label}${icon('external')}</a>`;
const section = (id, title, content) => `<section class="doc-section" id="${id}"><h2>${title}</h2>${content}</section>`;
const checkList = (items) => `<ul class="check-list">${items.map((item) => `<li>${icon('check')}<span>${item}</span></li>`).join('')}</ul>`;
function demoUrl(component = 'form') {
  const url = new URL('./demo/index.html', location.href.split('#')[0]);
  url.searchParams.set('component', component);
  url.searchParams.set('theme', document.documentElement.dataset.theme || 'light');
  return url.href;
}
function playground(component, code) {
  const title = component === 'form' ? '完整表单' : components[component].title;
  const url = demoUrl(component);
  return `<div class="playground" id="preview"><div class="playground-toolbar">
    <div class="tabs" role="tablist" aria-label="${title}示例视图"><button class="tab" id="tab-preview" role="tab" aria-selected="true" aria-controls="panel-preview" data-tab="preview">交互预览</button><button class="tab" id="tab-code" role="tab" aria-selected="false" aria-controls="panel-code" tabindex="-1" data-tab="code">使用代码</button></div>
    <div class="preview-tools"><button class="icon-button" data-width-button="phone" aria-pressed="true" aria-label="手机宽度" title="手机宽度">${icon('phone')}</button><button class="icon-button" data-width-button="wide" aria-pressed="false" aria-label="填满预览区域" title="填满预览区域">${icon('wide')}</button><span class="tool-separator" aria-hidden="true"></span><button class="icon-button" data-reload aria-label="重置预览" title="重置预览">${icon('reload')}</button><a class="icon-button preview-open" href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer" aria-label="在新页面打开预览" title="在新页面打开预览">${icon('external')}</a></div>
    </div><div id="panel-preview" role="tabpanel" aria-labelledby="tab-preview"><div class="preview-stage${component !== 'form' ? ' component-stage' : ''}"><div class="preview-device" data-width="phone"><iframe src="${escapeHtml(url)}" title="${title}：真实 Compose 交互预览" data-component="${component}" allow="clipboard-write"></iframe></div></div><div class="preview-caption"><span>与移动端共享 Kotlin 组件</span><a href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer" class="preview-open-text">在独立页面体验${icon('external')}</a></div></div><div id="panel-code" class="code-tab" role="tabpanel" aria-labelledby="tab-code" hidden>${codeBlock(code)}</div></div>`;
}
function overview() {
  const example = `import androidx.compose.runtime.Composable
import com.shadcn.mobile.App

@Composable
fun WorkspaceDemo() {
    // 完整表单自带主题切换与可恢复的表单状态
    App()
}`;
  return `<div class="page-intro"><h1>熟悉的设计，原生的交互。</h1><p class="lead">面向 Android 与 iOS，延续 shadcn 的简洁风格。<br>在完整表单中体验六个共享的 Compose 组件。</p><div class="intro-links">${buttonLink('installation', '开始使用', true)}${externalLink(REPOSITORY, 'GitHub 仓库', 'text-button')}<span class="subtle-label">${icon('layers')}Compose Multiplatform</span></div></div>
    ${playground('form', example)}
    <p class="preview-note">填写信息、搜索角色、调整通知偏好，再确认创建，数据只保留在当前演示中。网页预览可以体验组件外观和表单流程；系统键盘、返回手势和辅助功能仍需在原生 App 中验证。</p>
    ${section('components', '六个组件，一个完整流程。', `<p>点击组件，查看独立预览、使用示例与当前 Kotlin API。</p><div class="component-list">${Object.entries(components).map(([key, entry]) => `<a class="component-row" href="#/components/${key}"><strong>${entry.title}</strong><span>${entry.summary}</span>${icon('chevron')}</a>`).join('')}</div>`)}
    ${section('approach', '保留设计语言，遵循平台习惯。', `<p>颜色、边框和留白延续 shadcn 的风格，状态、内容插槽和事件回调采用 Compose 的写法。你可以直接修改源码。</p>${checkList(['Zinc 语义配色，浅色与深色主题。', '至少 48dp 的交互区域，支持字体缩放。', '明确的焦点、选择、加载与错误状态。'])}`)}
    ${section('status', '现在可以用它验证什么？', '<p>Android 原型已在模拟器验证表单流程、键盘、弹层关闭、主题切换和状态恢复。iOS 已在 iPhone 17 Pro（iOS 26.5）模拟器通过 8 项原生交互测试，覆盖中文表单、键盘、角色搜索、偏好设置、弹窗、主题切换和重置，0 失败、0 跳过且未重试。<a href="https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39112862050" target="_blank" rel="noopener noreferrer">查看 CI 记录</a>。</p><p>iOS 真机、VoiceOver 人工检查和大字号仍未验证。项目目前是可运行的早期原型，尚未发布 Maven 组件包。Combobox 和 Sheet 仍使用演示场景的数据与内容。</p><p>详细验证记录见项目中的 <code>docs/verification.md</code>，模拟器截图位于 <code>docs/screenshots/</code>。</p>')}`;
}
function installation() {
  const useExample = `import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.shadcn.mobile.ui.*

@Composable
fun ProfileForm() {
    var name by rememberSaveable { mutableStateOf("") }

    ShadcnTheme {
        SInput(
            value = name,
            onValueChange = { name = it },
            label = "你的名字",
            placeholder = "怎么称呼你？",
        )
    }
}`;
  return `<div class="page-intro"><h1>快速开始</h1><p class="lead">先在设备上运行原型，再将需要的组件用到你的项目中。目前提供源码，尚未发布依赖包。</p></div>
    ${section('clone', '获取源码', `<p>使用下面的命令克隆仓库。已有本地项目的读者可以直接跳到运行步骤。</p>${codeBlock('git clone https://github.com/zhanghan-13/shadcn-mobile.git\ncd shadcn-mobile', 'Terminal', 'shell')}<p>共享组件位于 <code>composeApp/src/commonMain</code>，Android 与 iOS 各自使用原生宿主。</p>`)}
    ${section('android', '运行 Android', `<p>使用 Android Studio 打开仓库根目录，安装 Gradle 配置指定的 Android SDK，然后运行 <code>androidApp</code>。</p>${checkList(['Android Studio 与兼容 Gradle wrapper 的 JDK。', 'Android SDK Platform 37，应用最低支持 API 24。', '一个已启动的 Android 模拟器，或开启调试的真机。'])}${codeBlock('./gradlew :androidApp:assembleDebug\n./gradlew :composeApp:testAndroidHostTest\n./gradlew :androidApp:connectedDebugAndroidTest', 'Terminal', 'shell')}<p>Windows 使用 <code>gradlew.bat</code>。构建结果位于 <code>androidApp/build/outputs/apk/debug/androidApp-debug.apk</code>。</p><h3>Windows 运行辅助脚本</h3>${codeBlock('.\\scripts\\android.ps1 -Action Build\n.\\scripts\\android.ps1 -Action Run -Serial emulator-5582', 'PowerShell', 'shell')}<p>将示例 serial 替换为 <code>adb devices</code> 显示的目标模拟器编号。</p>`)}
    ${section('ios', '运行 iOS', `<p>在 Apple silicon Mac 上使用 Xcode 打开 <code>iosApp/iosApp.xcodeproj</code>，选择共享的 <code>iosApp</code> scheme 与 iPhone 模拟器，然后运行。</p>${checkList(['macOS、Xcode 与兼容的 JDK。', 'iOS 16 或更新系统。', '模拟器使用 iosSimulatorArm64，真机使用 iosArm64。'])}${codeBlock('chmod +x gradlew\nopen iosApp/iosApp.xcodeproj', 'Terminal · macOS', 'shell')}<div class="note"><strong>iOS 验证范围</strong><p>2026-10-18 的 <a href="https://github.com/zhanghan-13/shadcn-mobile/actions/runs/39112862050" target="_blank" rel="noopener noreferrer">macOS CI</a> 使用 macOS 26.6.2、Xcode 26.4.1 与 Swift 6.3.1，链接真机和模拟器 framework、编译 Swift 宿主，并在 iPhone 17 Pro（iOS 26.5）模拟器通过全部 8 项原生交互测试，0 失败、0 跳过且未重试。真机、VoiceOver 人工检查和大字号仍未验证。</p></div>`)}
    ${section('use', '使用共享组件', `<p>项目中的组件位于 <code>com.shadcn.mobile.ui</code>。在主题下组合它们，并把状态保留在调用方。</p>${codeBlock(useExample)}<p>独立组件示例默认放在 <code>ShadcnTheme</code> 中。根据使用情况导入 Compose 的布局、键盘与状态 API。</p>`)}
    ${section('structure', '目录结构', `${codeBlock('composeApp/\n  src/commonMain/    # 共享主题、组件、表单与校验\n  src/commonTest/    # 共享逻辑测试\n  src/iosMain/       # iOS UIViewController 入口\nandroidApp/         # Android App 与交互测试\niosApp/             # SwiftUI / UIKit 宿主\ndocs/               # 验证记录与模拟器截图', 'Project', 'text')}<p>完整项目说明见本地 <code>README.md</code>。</p>`)}`;
}
function theming() {
  return `<div class="page-intro"><h1>主题与令牌</h1><p class="lead">表单、按钮和弹层共用一套语义颜色。组件按用途读取颜色，无需各自定义。</p></div>
    ${section('theme', '从 ShadcnTheme 开始', `<p>默认跟随系统外观，也可以显式传入 <code>darkTheme</code>。字体沿用平台 sans，Material3 颜色同步映射到相同语义。</p>${codeBlock('ShadcnTheme(darkTheme = false) {\n    SButton("开始使用", onClick = { /* 你的操作 */ })\n}\n\n// 在任意子组件中读取语义颜色\nval colors = ShadcnTheme.colors\nText("工作空间", color = colors.foreground)')}<p>右上角主题按钮切换本站文档，并重新载入组件预览。完整表单也可使用其内部的主题开关。</p>`)}
    ${section('colors', '颜色令牌', `<div class="table-scroll"><table><thead><tr><th>令牌</th><th>浅色</th><th>深色</th><th>用途</th></tr></thead><tbody>${tokens.map(([name, light, dark, purpose]) => `<tr><td><code>${name}</code></td><td><span class="token-value"><i class="token-swatch" style="background:${light}" aria-hidden="true"></i><code>${light}</code></span></td><td><span class="token-value"><i class="token-swatch" style="background:${dark}" aria-hidden="true"></i><code>${dark}</code></span></td><td>${purpose}</td></tr>`).join('')}</tbody></table></div>`)}
    ${section('customize', '调整设计语言', `<p>编辑 <code>ui/Theme.kt</code> 中的 LightColors、DarkColors 与 ShadcnTypography。颜色通过 <code>LocalShadcnColors</code> 和 <code>ShadcnTheme.colors</code> 读取。</p><p>若改动配色，请同时检查深浅主题的文字对比度、焦点、错误与禁用状态。修改主题源码可以保持自定义令牌与 Material3 弹层颜色一致。</p>${buttonLink('installation', '查看接入方式')}`)}
    ${section('geometry', '尺寸与字体', '<div class="table-scroll"><table><thead><tr><th>项目</th><th>约定</th></tr></thead><tbody><tr><td>交互区域</td><td>至少 48dp，文字容器随字体增长</td></tr><tr><td>按钮 / 输入框</td><td>8dp 圆角，细边框</td></tr><tr><td>主要文字</td><td>平台 SansSerif；输入文字 16sp</td></tr><tr><td>标签 / 按钮</td><td>14sp，中等字重</td></tr><tr><td>表单留白</td><td>24dp 外侧留白，20–24dp 组件间距</td></tr></tbody></table></div><p>布局保持紧凑，同时为触控和放大的文字留出空间。</p>')}`;
}
function componentPage(key) {
  const component = components[key];
  return `<div class="page-intro"><h1>${component.title}</h1><p class="lead">${component.description}</p><div class="intro-links"><a class="text-button" href="#/components/${key}?section=api" data-section="api">查看 Kotlin API${icon('code')}</a><span class="subtle-label">${icon('code')}${component.name}</span></div></div>
    ${playground(key, component.sample)}<p class="preview-note">预览运行共享的 Compose 实现。系统键盘、返回手势和辅助功能行为，以原生设备验证为准。</p>
    ${section('usage', '使用方式', `${component.note ? `<div class="note">${component.note}</div>` : ''}<p>在 <code>ShadcnTheme</code> 下调用，状态和事件由你的业务代码管理。</p>${codeBlock(component.sample)}`)}
    ${section('behavior', '交互约定', `<ul>${component.behavior.map((item) => `<li>${item}</li>`).join('')}</ul>`)}
    ${section('api', 'API 参考', `<p>以下参数与仓库中的当前实现一致。</p><div class="table-scroll"><table class="api-table"><thead><tr><th>参数</th><th>类型 / 默认值</th><th>说明</th></tr></thead><tbody>${component.props.map(([name, type, description]) => `<tr><td><code>${escapeHtml(name)}</code></td><td><code>${escapeHtml(type)}</code></td><td>${description}</td></tr>`).join('')}</tbody></table></div><details class="signature"><summary>完整函数签名</summary>${codeBlock(component.signature)}</details>`)}
    ${section('native', '在移动端验证', '<p>浏览器可用于快速体验组件状态。接入 App 前，请检查实际设备上的触控、键盘遮挡、字体缩放与辅助功能。<a href="#/installation">查看原生运行方式</a>。</p>')}`;
}
function renderNavigation() {
  $('#side-navigation').innerHTML = ['开始使用', '组件'].map((category) => `<div class="nav-group"><p class="nav-heading">${category}</p>${pages.filter((page) => page.category === category).map((page) => `<a class="nav-link" href="#/${page.route}"${page.route === currentRoute ? ' aria-current="page"' : ''}><span>${page.title}</span>${page.zh ? `<span class="nav-native">${page.zh}</span>` : ''}</a>`).join('')}</div>`).join('');
  $$('[data-top-nav]').forEach((link) => {
    const selected = currentRoute.startsWith('components/') ? link.dataset.topNav === 'components' : link.dataset.topNav === 'docs';
    if (selected) link.setAttribute('aria-current', 'page'); else link.removeAttribute('aria-current');
  });
}
function renderRoute(initial = false) {
  const route = location.hash.replace(/^#\/?/, '').split('?')[0] || 'overview';
  if (route === 'main') { $('#main').focus(); return; }
  const page = pages.find((candidate) => candidate.route === route);
  currentRoute = page ? route : 'not-found';
  codeValues.clear();
  let content;
  if (!page) content = `<div class="page-intro"><h1>这一页还不存在。</h1><p class="lead">可以从目录中选择一个组件，或回到完整表单继续体验。</p><div class="intro-links">${buttonLink('overview', '回到文档首页', true)}</div></div>`;
  else if (route === 'overview') content = overview();
  else if (route === 'installation') content = installation();
  else if (route === 'theming') content = theming();
  else content = componentPage(route.split('/')[1]);
  const index = pages.findIndex((entry) => entry.route === currentRoute);
  const previous = index > 0 ? pages[index - 1] : null;
  const next = index >= 0 && index < pages.length - 1 ? pages[index + 1] : null;
  const footer = page ? `<nav class="page-footer" aria-label="上一篇与下一篇">${previous ? `<a href="#/${previous.route}"><small>上一篇</small><span>${previous.title}</span></a>` : ''}${next ? `<a href="#/${next.route}"><small>下一篇</small><span>${next.title}</span></a>` : ''}</nav>` : '';
  $('#main').innerHTML = `<div class="breadcrumb"><a href="#/overview">文档</a>${icon('chevron')}<span>${page?.title || '未找到'}</span></div>${content}${footer}<p class="site-note">社区开发的 Compose 原型，与 shadcn/ui 官方无隶属关系。<br><a href="${REPOSITORY}" target="_blank" rel="noopener noreferrer">GitHub 仓库</a> · Android 与 iOS</p>`;
  renderNavigation();
  $('#toc-content').innerHTML = `<p class="toc-label">本页内容</p>${$$('.doc-section', $('#main')).map((entry) => `<a href="#/${currentRoute}?section=${entry.id}" data-section="${entry.id}">${$('h2', entry).textContent}</a>`).join('')}<div class="toc-separator"></div>${externalLink(`${REPOSITORY}/issues`, '反馈问题', 'toc-external')}<a href="#/overview?section=status" class="toc-external">测试与验证${icon('chevron')}</a>`;
  document.title = `${page?.title || '未找到'} — shadcn-mobile`;
  closeMenu(false);
  window.scrollTo({ top: 0, behavior: 'instant' });
  if (!initial) $('#main').focus({ preventScroll: true });
  const anchor = new URLSearchParams(location.hash.split('?')[1] || '').get('section');
  if (anchor) document.getElementById(anchor)?.scrollIntoView({ behavior: 'instant', block: 'start' });
}
function notify(message) {
  const toast = $('#toast');
  clearTimeout(toastTimer);
  toast.textContent = message;
  toast.hidden = false;
  toastTimer = setTimeout(() => { toast.hidden = true; }, 2500);
}
async function copyCode(button) {
  const value = codeValues.get(button.dataset.copy);
  if (value == null) return;
  try {
    if (!navigator.clipboard?.writeText) throw new Error('Clipboard unavailable');
    await navigator.clipboard.writeText(value);
    button.innerHTML = `${icon('check')}<span>已复制</span>`;
    notify('代码已复制');
    setTimeout(() => { if (button.isConnected) button.innerHTML = `${icon('copy')}<span>复制</span>`; }, 2000);
  } catch {
    const code = button.closest('.code-block').querySelector('code');
    const selection = window.getSelection();
    const range = document.createRange();
    range.selectNodeContents(code);
    selection.removeAllRanges(); selection.addRange(range);
    notify('已选中代码，请按 Ctrl+C 或 ⌘C 复制');
  }
}
function updateThemeButton() {
  const dark = document.documentElement.dataset.theme === 'dark';
  $('#theme-toggle').innerHTML = icon(dark ? 'sun' : 'moon');
  $('#theme-toggle').setAttribute('aria-label', `切换到${dark ? '浅' : '深'}色主题`);
  $('meta[name="theme-color"]').content = dark ? '#09090b' : '#ffffff';
}
function toggleTheme() {
  const theme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
  document.documentElement.dataset.theme = theme;
  try { localStorage.setItem('shadcn-mobile-docs-theme', theme); } catch { /* Storage is optional. */ }
  updateThemeButton();
  const frame = $('iframe[data-component]');
  if (frame) {
    frame.src = demoUrl(frame.dataset.component);
    $$('.preview-open, .preview-open-text').forEach((link) => { link.href = frame.src; });
  }
}
function closeMenu(restoreFocus = true) {
  $('#sidebar').dataset.open = 'false';
  $('#sidebar').removeAttribute('role');
  $('#sidebar').removeAttribute('aria-modal');
  $('#sidebar-shade').hidden = true;
  $('#menu-toggle').setAttribute('aria-expanded', 'false');
  document.body.style.overflow = '';
  if (restoreFocus) $('#menu-toggle').focus();
}
function openMenu() {
  $('#sidebar').dataset.open = 'true';
  $('#sidebar').setAttribute('role', 'dialog');
  $('#sidebar').setAttribute('aria-modal', 'true');
  $('#sidebar-shade').hidden = false;
  $('#menu-toggle').setAttribute('aria-expanded', 'true');
  document.body.style.overflow = 'hidden';
  $('#menu-close').focus();
}
function updateSearch() {
  const results = searchPages($('#search-input').value);
  searchIndex = 0;
  $('#search-results').innerHTML = results.length ? results.map((page, index) => `<a href="#/${page.route}" class="search-result" id="search-option-${index}" role="option" aria-selected="${index === 0}" data-active="${index === 0}"><div><strong>${page.title}${page.zh ? ` <span class="nav-native">${page.zh}</span>` : ''}</strong><small>${page.description}</small></div>${icon('arrow')}</a>`).join('') : '<p class="search-empty" role="status">没有找到相关文档。试试“按钮”“主题”或“Android”。</p>';
  if (results.length) $('#search-input').setAttribute('aria-activedescendant', 'search-option-0'); else $('#search-input').removeAttribute('aria-activedescendant');
}
function openSearch() { closeMenu(false); $('#search-input').value = ''; updateSearch(); $('#search-dialog').showModal(); $('#search-input').setAttribute('aria-expanded', 'true'); $('#search-input').focus(); }
function closeSearch() { $('#search-dialog').close(); $('#search-input').setAttribute('aria-expanded', 'false'); $('#search-trigger').focus(); }
function switchTab(name) {
  $$('[data-tab]').forEach((tab) => { const selected = tab.dataset.tab === name; tab.setAttribute('aria-selected', String(selected)); tab.tabIndex = selected ? 0 : -1; });
  $('#panel-preview').hidden = name !== 'preview'; $('#panel-code').hidden = name !== 'code'; $('.preview-tools').hidden = name !== 'preview';
}
document.addEventListener('click', (event) => {
  const copy = event.target.closest('[data-copy]'); if (copy) { copyCode(copy); return; }
  const tab = event.target.closest('[data-tab]'); if (tab) { switchTab(tab.dataset.tab); return; }
  const width = event.target.closest('[data-width-button]');
  if (width) { $('.preview-device').dataset.width = width.dataset.widthButton; $$('[data-width-button]').forEach((button) => button.setAttribute('aria-pressed', String(button === width))); return; }
  if (event.target.closest('[data-reload]')) { const frame = $('iframe[data-component]'); frame.src = demoUrl(frame.dataset.component); notify('预览已重置'); return; }
  const sectionLink = event.target.closest('[data-section]');
  if (sectionLink) {
    if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
    event.preventDefault();
    history.replaceState(null, '', sectionLink.href);
    document.getElementById(sectionLink.dataset.section)?.scrollIntoView({ behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth', block: 'start' });
    return;
  }
  const result = event.target.closest('.search-result'); if (result) closeSearch();
  const sameRoute = event.target.closest('a[href^="#/"]');
  if (sameRoute && sameRoute.hash === location.hash) { closeMenu(false); if (result) $('#main').focus(); }
});
document.addEventListener('keydown', (event) => {
  if ((event.metaKey || event.ctrlKey) && event.key.toLocaleLowerCase() === 'k') { event.preventDefault(); if ($('#search-dialog').open) closeSearch(); else openSearch(); return; }
  if (event.key === 'Escape' && $('#sidebar').dataset.open === 'true') { closeMenu(); return; }
  if (event.target.matches('[data-tab]') && ['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
    event.preventDefault(); const name = event.key === 'Home' ? 'preview' : event.key === 'End' ? 'code' : event.target.dataset.tab === 'preview' ? 'code' : 'preview'; switchTab(name); $(`[data-tab="${name}"]`).focus();
  }
  if ($('#sidebar').dataset.open === 'true' && event.key === 'Tab') {
    const elements = $$('button, a', $('#sidebar')); const first = elements[0], last = elements.at(-1);
    if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); } else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
  }
});
$('#search-input').addEventListener('input', updateSearch);
$('#search-input').addEventListener('keydown', (event) => {
  const results = $$('.search-result'); if (!results.length) return;
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault(); searchIndex = (searchIndex + (event.key === 'ArrowDown' ? 1 : -1) + results.length) % results.length;
    results.forEach((result, index) => { result.dataset.active = String(index === searchIndex); result.setAttribute('aria-selected', String(index === searchIndex)); }); $('#search-input').setAttribute('aria-activedescendant', results[searchIndex].id); results[searchIndex].scrollIntoView({ block: 'nearest' });
  } else if (event.key === 'Enter') { event.preventDefault(); results[searchIndex]?.click(); }
});
$('#search-dialog').addEventListener('click', (event) => { if (event.target === $('#search-dialog')) closeSearch(); });
$('#search-dialog').addEventListener('cancel', (event) => { event.preventDefault(); closeSearch(); });
$('#search-trigger').addEventListener('click', openSearch);
$('#search-close').addEventListener('click', closeSearch);
$('#theme-toggle').addEventListener('click', toggleTheme);
$('#menu-toggle').addEventListener('click', openMenu);
$('#menu-close').innerHTML = icon('close');
$('#menu-close').addEventListener('click', () => closeMenu());
$('#sidebar-shade').addEventListener('click', () => closeMenu());
window.addEventListener('hashchange', () => renderRoute());
window.addEventListener('resize', () => { if (window.innerWidth > 700 && $('#sidebar').dataset.open === 'true') closeMenu(false); });
if (!navigator.platform?.toLowerCase().includes('mac')) $('.search-trigger kbd').textContent = 'Ctrl K';
updateThemeButton();
renderRoute(true);

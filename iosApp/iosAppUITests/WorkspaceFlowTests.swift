import UIKit
import XCTest

/// Exercises the shipped Swift host through iOS accessibility and system input.
/// No app state, Compose semantics actions, or test-only launch data are injected.
final class WorkspaceFlowTests: XCTestCase {
    @MainActor private var application: XCUIApplication?

    @MainActor
    override func setUp() async throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        // Keep system edit/keyboard commands predictable; the app's Chinese copy is unchanged.
        app.launchArguments = ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        application = app
        app.launch()
        XCTAssertTrue(element("workspace_name").waitForExistence(timeout: 20))
    }

    @MainActor
    override func tearDown() async throws {
        if let app = application {
            if (testRun?.totalFailureCount ?? 0) > 0 {
                capture("failure")
                let tree = XCTAttachment(string: app.debugDescription)
                tree.name = "failure-accessibility-tree"
                tree.lifetime = .keepAlways
                add(tree)
            }
            app.terminate()
        }
        UIPasteboard.general.items = []
        application = nil
    }

    @MainActor
    func testEmptySubmitShowsRequiredErrors() {
        tap("submit_button", scrolling: .up)
        expectLabel("workspace_name_error", "请输入工作空间名称")
        expectLabel("email_error", "请输入工作邮箱")
        reveal("error_role", direction: .up)
        expectLabel("error_role", "请选择你的角色")
        reveal("terms_checkbox", direction: .up)
        // Checkbox descendants are merged in the native accessibility tree.
        expect(element("terms_checkbox"), "label CONTAINS %@", "请先阅读并同意演示条款")
        expectAbsent("review_confirm")
        expectAbsent("create_another")
        capture("required-errors")
    }

    @MainActor
    func testInvalidEmailCannotOpenReview() {
        enterDetails(name: "Validation workspace", email: "not-an-email")
        selectDeveloper()
        tap("terms_checkbox", scrolling: .up)
        expectSelected("terms_checkbox", true)
        tap("submit_button", scrolling: .up)
        expectLabel("email_error", "请输入有效的邮箱地址")
        expectAbsent("review_confirm")
        expectAbsent("create_another")
        capture("invalid-email")
    }

    @MainActor
    func testCompleteChineseWorkspaceReviewAndReset() {
        capture("01-form")
        pasteChineseWorkspaceName()
        type("email", "hello@example.com")
        pressKeyboardButton(["Done", "done", "完成"])
        expectKeyboardHidden()
        selectDeveloper()

        tap("preferences_button", scrolling: .up)
        expectSelected("email_updates", true)
        expectSelected("product_news", false)
        tap("product_news")
        expectSelected("product_news", true)
        capture("03-preferences-saved")
        tap("save_preferences")
        expectAbsent("save_preferences")
        expect(element("preferences_button"), "label CONTAINS %@", "工作空间更新与产品动态")
        tap("terms_checkbox", scrolling: .up)
        expectSelected("terms_checkbox", true)

        tap("submit_button", scrolling: .up)
        expectSummary(name: "设计工作室", preferences: "工作空间更新与产品动态")
        capture("04-review")
        tap("review_cancel")
        expectAbsent("review_confirm")
        reveal("workspace_name", direction: .down)
        expectValue("workspace_name", "设计工作室")
        expectValue("email", "hello@example.com")
        reveal("terms_checkbox", direction: .up)
        expectSelected("terms_checkbox", true)
        tap("preferences_button", scrolling: .down)
        expectSelected("product_news", true)
        tap("save_preferences")

        tap("submit_button", scrolling: .up)
        tap("review_confirm")
        XCTAssertTrue(element("create_another").waitForExistence(timeout: 10))
        expectAbsent("review_confirm")
        expectSummary(name: "设计工作室", preferences: "工作空间更新与产品动态")
        capture("05-success")
        tap("create_another", scrolling: .up)
        expectAbsent("create_another")
        expectValue("workspace_name", "")
        expectValue("email", "")
        reveal("terms_checkbox", direction: .up)
        expectSelected("terms_checkbox", false)
        tap("preferences_button", scrolling: .down)
        expectSelected("email_updates", true)
        expectSelected("product_news", false)
    }

    @MainActor
    func testRoleSearchRecoversFromNoResults() {
        tap("role_trigger", scrolling: .up)
        type("role_search", "astronaut")
        XCTAssertTrue(element("role_empty").waitForExistence(timeout: 5))
        expectAbsent("role_option_developer")
        // The caret remains at the end of the text just typed, so native deletes are deterministic.
        element("role_search").typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: "astronaut".count))
        element("role_search").typeText("Developer")
        expectValue("role_search", "Developer")
        expectAbsent("role_empty")
        tap("role_option_developer")
        expectAbsent("role_search")
        expectValue("role_trigger", "已收起")
        tap("submit_button", scrolling: .up)
        expectLabel("workspace_name_error", "请输入工作空间名称")
        expectAbsent("error_role")
        capture("role-search-recovered")
    }

    @MainActor
    func testPreferencesCancelAndSwipeDiscardUnsavedChanges() {
        tap("preferences_button", scrolling: .up)
        expectSelected("product_news", false)
        tap("product_news")
        expectSelected("product_news", true)
        tap("cancel_preferences")
        expectAbsent("save_preferences")
        expect(element("preferences_button"), "label CONTAINS %@", "仅接收工作空间更新")

        tap("preferences_button")
        expectSelected("product_news", false)
        tap("product_news")
        expectSelected("product_news", true)
        let handle = element("preferences_drag_handle")
        let title = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", "只接收你关心的内容")).element
        let dragSource = handle.exists && !handle.frame.isEmpty ? handle : title
        XCTAssertTrue(dragSource.waitForExistence(timeout: 5))
        dragSource.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
            .press(forDuration: 0.15, thenDragTo: app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.96)))
        expectAbsent("save_preferences")
        tap("preferences_button")
        expectSelected("product_news", false)
        expectSelected("email_updates", true)
        capture("preferences-discarded")
    }

    @MainActor
    func testKeyboardNextAndDonePreserveTypedValues() {
        type("workspace_name", "Keyboard workspace")
        pressKeyboardButton(["Next", "next", "下一项", "下一个"])
        // Do not tap email: typing into the active application proves Next moved focus.
        app.typeText("keyboard@example.com")
        expectValue("email", "keyboard@example.com")
        expectValue("workspace_name", "Keyboard workspace")
        capture("keyboard-next")
        pressKeyboardButton(["Done", "done", "完成"])
        expectKeyboardHidden()
        expectValue("email", "keyboard@example.com")
        expectValue("workspace_name", "Keyboard workspace")
    }

    @MainActor
    func testThemeTogglePreservesDraftAndSelection() {
        enterDetails(name: "Theme workspace", email: "hello@example.com")
        selectDeveloper()
        tap("terms_checkbox", scrolling: .up)
        expectSelected("terms_checkbox", true)
        let previousThemeLabel = element("theme_toggle").label
        tap("theme_toggle")
        expect(element("theme_toggle"), "label != %@", previousThemeLabel)
        reveal("workspace_name", direction: .down)
        expectValue("workspace_name", "Theme workspace")
        expectValue("email", "hello@example.com")
        capture("06-theme-preserved")
        reveal("role_trigger", direction: .up)
        expectValue("role_trigger", "已收起")
        reveal("terms_checkbox", direction: .up)
        expectSelected("terms_checkbox", true)
        tap("submit_button", scrolling: .up)
        expectSummary(name: "Theme workspace", preferences: "仅接收工作空间更新")
    }

    @MainActor
    func testTermsDialogClosesWithoutChangingDraft() {
        enterDetails(name: "Terms workspace", email: "terms@example.com")
        tap("terms_button", scrolling: .up)
        XCTAssertTrue(element("terms_close").waitForExistence(timeout: 5))
        capture("terms-dialog")
        tap("terms_close")
        expectAbsent("terms_close")
        reveal("workspace_name", direction: .down)
        expectValue("workspace_name", "Terms workspace")
        expectValue("email", "terms@example.com")
        reveal("terms_checkbox", direction: .up)
        expectSelected("terms_checkbox", false)
    }

    @MainActor
    func testSwitchTogglesOnceAndIgnoresDisabledTap() {
        openComponent("switch")
        expectEnabled("switch_updates", true)
        expectSelected("switch_updates", false)
        expectEnabled("switch_disabled", false)
        expectSelected("switch_disabled", true)
        expectLabel("switch_count", "已切换 0 次")

        tap("switch_updates")
        expectSelected("switch_updates", true)
        expectLabel("switch_count", "已切换 1 次")
        // Exercise both the labeled row and the trailing visual switch with native touches.
        element("switch_updates").coordinate(withNormalizedOffset: CGVector(dx: 0.95, dy: 0.5)).tap()
        expectSelected("switch_updates", false)
        expectLabel("switch_count", "已切换 2 次")

        tapDisabledControl("switch_disabled")
        expectSelected("switch_disabled", true)
        expectSelected("switch_updates", false)
        expectLabel("switch_count", "已切换 2 次")
        capture("switch-disabled-and-callback-count")
    }

    @MainActor
    func testTextareaAcceptsChineseMultilineAndProtectsNoneditableValues() {
        openComponent("textarea")
        expectValue("textarea_notes", "")
        tap("textarea_validate", scrolling: .up)
        reveal("textarea_notes_error", direction: .down)
        expectLabel("textarea_notes_error", "请填写项目说明。")
        reveal("textarea_notes", direction: .down)

        let pastedLines = "第一行中文\n第二行中文"
        paste(pastedLines, into: "textarea_notes")
        expectAbsent("textarea_notes_error")
        // A real return key appends a third line; Paste alone cannot prove multiline editing.
        let keyboardLine = "\nKeyboard third line"
        element("textarea_notes").typeText(keyboardLine)
        let completeNotes = pastedLines + keyboardLine
        expectValue("textarea_notes", completeNotes)
        capture("textarea-chinese-and-keyboard-newlines")

        tap("textarea_readonly", scrolling: .up)
        expectKeyboardHidden()
        expectEnabled("textarea_readonly", true)
        expectValue("textarea_readonly", "这段说明可以选择和复制。\n只读内容不会被编辑。")
        expectValue("textarea_notes", completeNotes)

        tapDisabledControl("textarea_disabled")
        expectKeyboardHidden()
        expectValue("textarea_disabled", "当前不可编辑")
        expectValue("textarea_readonly", "这段说明可以选择和复制。\n只读内容不会被编辑。")
        expectValue("textarea_notes", completeNotes)
        capture("textarea-readonly-and-disabled")
    }

    @MainActor
    func testRadioGroupKeepsOneSelectionAndIgnoresDisabledTap() {
        openComponent("radio-group")
        expectSelected("radio_plan_free", true)
        expectSelected("radio_plan_pro", false)
        expectSelected("radio_plan_disabled", false)
        expectEnabled("radio_plan_disabled", false)
        expectLabel("radio_selected", "当前方案：免费版")
        expectLabel("radio_count", "已切换 0 次")

        tap("radio_plan_pro", scrolling: .up)
        expectSelected("radio_plan_free", false)
        expectSelected("radio_plan_pro", true)
        expectSelected("radio_plan_disabled", false)
        expectLabel("radio_selected", "当前方案：专业版")
        expectLabel("radio_count", "已切换 1 次")
        tapDisabledControl("radio_plan_disabled")
        expectSelected("radio_plan_free", false)
        expectSelected("radio_plan_pro", true)
        expectSelected("radio_plan_disabled", false)
        expectLabel("radio_selected", "当前方案：专业版")
        expectLabel("radio_count", "已切换 1 次")

        tap("radio_plan_free", scrolling: .down)
        expectSelected("radio_plan_free", true)
        expectSelected("radio_plan_pro", false)
        expectSelected("radio_plan_disabled", false)
        expectLabel("radio_selected", "当前方案：免费版")
        expectLabel("radio_count", "已切换 2 次")
        capture("radio-exclusive-selection")
    }

    @MainActor
    func testAlertUpdatesContentAfterRetry() {
        openComponent("alert")
        expectAlert("alert_default", title: "本地演示",
                    description: "这里的更改只保存在当前设备，不会提交到服务器。")
        expectAlert("alert_destructive", title: "无法保存更改",
                    description: "连接已断开。检查网络后再试一次。")
        expectLabel("alert_trigger", "重试保存")
        expectEnabled("alert_trigger", true)
        capture("alert-before-retry")

        tap("alert_trigger", scrolling: .up)
        expectAlert("alert_destructive", title: "已重新连接", description: "现在可以继续编辑。")
        expectAlert("alert_default", title: "本地演示",
                    description: "这里的更改只保存在当前设备，不会提交到服务器。")
        expectLabel("alert_trigger", "连接已恢复")
        expectEnabled("alert_trigger", false)
        capture("alert-reconnected")
    }

    @MainActor
    func testComponentNavigationPreservesFormDraftAndTheme() {
        enterDetails(name: "Navigation workspace", email: "hello@example.com")
        selectDeveloper()
        tap("preferences_button", scrolling: .up)
        tap("product_news")
        expectSelected("product_news", true)
        tap("save_preferences")
        tap("terms_checkbox", scrolling: .up)
        expectSelected("terms_checkbox", true)
        let previousThemeLabel = element("theme_toggle").label
        tap("theme_toggle")
        expect(element("theme_toggle"), "label != %@", previousThemeLabel)
        let selectedThemeLabel = element("theme_toggle").label

        openComponent("switch")
        expectSelected("switch_updates", false)
        expectLabel("theme_toggle", selectedThemeLabel)
        tap("theme_toggle")
        expect(element("theme_toggle"), "label != %@", selectedThemeLabel)
        let detailThemeLabel = element("theme_toggle").label
        capture("native-component-detail")
        tap("component_back")
        XCTAssertTrue(element("catalog_back").waitForExistence(timeout: 5))
        expectLabel("theme_toggle", detailThemeLabel)
        tap("catalog_back")
        reveal("workspace_name", direction: .down)
        expectValue("workspace_name", "Navigation workspace")
        expectValue("email", "hello@example.com")
        expectLabel("theme_toggle", detailThemeLabel)
        reveal("terms_checkbox", direction: .up)
        expectSelected("terms_checkbox", true)
        tap("submit_button", scrolling: .up)
        expectSummary(name: "Navigation workspace", preferences: "工作空间更新与产品动态")
        capture("native-navigation-preserved-draft")
    }

    private enum ScrollDirection { case up, down }

    @MainActor private var app: XCUIApplication {
        guard let application else { preconditionFailure("Application must be launched in setUp") }
        return application
    }

    @MainActor private func element(_ tag: String) -> XCUIElement {
        // Compose may expose container tags on synthetic children. All actions/assertions
        // therefore use unique leaf identifiers and never scope through a tagged container.
        app.descendants(matching: .any).matching(NSPredicate(format: "identifier == %@", tag)).element
    }

    @MainActor private func reveal(_ tag: String, direction: ScrollDirection) {
        for _ in 0..<7 {
            let target = element(tag)
            if target.exists && target.isHittable { return }
            if element("component_back").exists {
                scrollComponent(direction)
            } else if direction == .up {
                app.swipeUp()
            } else {
                app.swipeDown()
            }
        }
        XCTAssertTrue(element(tag).exists && element(tag).isHittable, "Could not reveal \(tag)")
    }

    @MainActor private func tap(_ tag: String, scrolling direction: ScrollDirection? = nil) {
        if let direction { reveal(tag, direction: direction) }
        let target = element(tag)
        XCTAssertTrue(target.waitForExistence(timeout: 10), "Missing \(tag)")
        expect(target, "hittable == true")
        target.tap()
    }

    @MainActor private func openComponent(_ slug: String) {
        tap("open_components")
        tap("catalog_\(slug)", scrolling: .up)
        XCTAssertTrue(element("component_back").waitForExistence(timeout: 5))
    }

    @MainActor private func scrollComponent(_ direction: ScrollDirection) {
        let content = element("component_scroll")
        XCTAssertTrue(content.waitForExistence(timeout: 5))
        let screen = app.frame
        let frame = content.frame.intersection(screen)
        let top = max(frame.minY, element("component_back").frame.maxY)
        let keyboard = app.keyboards.element
        let bottom = keyboard.exists ? min(frame.maxY, keyboard.frame.minY) : frame.maxY
        let height = bottom - top
        XCTAssertGreaterThan(height, 80, "The component must expose a scrollable area above the keyboard")
        // Stay in the gallery's trailing padding, outside the Textarea's own scroll
        // surface and above the keyboard. The gesture uses the current AX geometry.
        let x = frame.maxX - 10 - screen.minX
        let upperY = top + height * 0.2 - screen.minY
        let lowerY = top + height * 0.8 - screen.minY
        let origin = app.coordinate(withNormalizedOffset: CGVector(dx: 0, dy: 0))
        let upper = origin.withOffset(CGVector(dx: x, dy: upperY))
        let lower = origin.withOffset(CGVector(dx: x, dy: lowerY))
        if direction == .up {
            lower.press(forDuration: 0.1, thenDragTo: upper)
        } else {
            upper.press(forDuration: 0.1, thenDragTo: lower)
        }
    }

    @MainActor private func tapDisabledControl(_ tag: String) {
        // Disabled elements need not be hittable. Send a real touch at their visible
        // accessibility frame, then let each test assert that state/callbacks did not change.
        for _ in 0..<7 {
            let target = element(tag)
            if target.exists && !target.frame.isEmpty && app.frame.contains(target.frame)
                && target.frame.minY > element("component_back").frame.maxY {
                expectEnabled(tag, false)
                target.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
                return
            }
            scrollComponent(.up)
        }
        XCTFail("Could not reveal disabled control \(tag) for a native touch")
    }

    @MainActor private func type(_ tag: String, _ text: String) {
        tap(tag)
        XCTAssertTrue(app.keyboards.element.waitForExistence(timeout: 5))
        dismissKeyboardIntroductionIfPresent()
        element(tag).typeText(text)
        expectValue(tag, text)
    }

    @MainActor private func enterDetails(name: String, email: String) {
        type("workspace_name", name)
        pressKeyboardButton(["Next", "next", "下一项", "下一个"])
        app.typeText(email)
        expectValue("email", email)
        pressKeyboardButton(["Done", "done", "完成"])
        expectKeyboardHidden()
    }

    @MainActor private func selectDeveloper() {
        tap("role_trigger", scrolling: .up)
        type("role_search", "Developer")
        tap("role_option_developer")
        expectAbsent("role_search")
        expectKeyboardHidden()
        expectValue("role_trigger", "已收起")
        capture("02-role-selected")
    }

    @MainActor private func pasteChineseWorkspaceName() {
        paste("设计工作室", into: "workspace_name")
        pressKeyboardButton(["Next", "next", "下一项", "下一个"])
    }

    @MainActor private func paste(_ text: String, into tag: String) {
        // A native Paste action exercises Unicode input without depending on a Pinyin
        // keyboard being installed on the CI simulator. It does not prefill app state.
        UIPasteboard.general.string = text
        tap(tag)
        XCTAssertTrue(app.keyboards.element.waitForExistence(timeout: 5))
        dismissKeyboardIntroductionIfPresent()
        element(tag).press(forDuration: 1.0)
        let pastePredicate = NSPredicate(format: "label IN %@", ["Paste", "粘贴"])
        let menuItem = app.menuItems.matching(pastePredicate).element
        let button = app.buttons.matching(pastePredicate).element
        if menuItem.waitForExistence(timeout: 2) {
            menuItem.tap()
        } else {
            XCTAssertTrue(button.waitForExistence(timeout: 3), "The system Paste command must be available")
            button.tap()
        }
        // Only approve the standard paste prompt explicitly naming this app.
        let pasteAlert = app.alerts.matching(NSPredicate(format: "label CONTAINS[c] 'paste' AND label CONTAINS[c] 'shadcn-mobile'")).element
        if pasteAlert.waitForExistence(timeout: 1) {
            let allowPaste = pasteAlert.buttons["Allow Paste"]
            XCTAssertTrue(allowPaste.exists)
            allowPaste.tap()
        }
        dismissKeyboardIntroductionIfPresent()
        expectValue(tag, text)
    }

    @MainActor private func pressKeyboardButton(_ labels: [String]) {
        dismissKeyboardIntroductionIfPresent()
        let key = app.keyboards.buttons.matching(NSPredicate(format: "label IN %@", labels)).element
        XCTAssertTrue(key.waitForExistence(timeout: 5), "Missing keyboard key: \(labels)")
        key.tap()
    }

    @MainActor private func dismissKeyboardIntroductionIfPresent() {
        // Observed on the fresh CI simulator. Scope to this native keyboard view,
        // and verify its explanatory copy before accepting its Continue action.
        let introduction = app.otherElements["UIContinuousPathIntroductionView"]
        guard introduction.waitForExistence(timeout: 1) else { return }
        let explanation = introduction.staticTexts[
            "Speed up your typing by sliding your finger across the letters to compose a word."
        ]
        XCTAssertTrue(explanation.waitForExistence(timeout: 3))
        let continueButton = introduction.buttons["Continue"]
        expect(continueButton, "exists == true AND hittable == true")
        continueButton.tap()
        expect(introduction, "exists == false")
    }

    @MainActor private func expectKeyboardHidden() {
        // On a fresh hosted simulator, the first keyboard dismissal can take longer
        // to reach the AX tree. Wait for the same state without repeating the action.
        expect(app.keyboards.element, "exists == false", timeout: 15)
    }

    @MainActor private func expectSummary(name: String, preferences: String) {
        expectLabel("summary_name", name)
        expectLabel("summary_email", "hello@example.com")
        expectLabel("summary_role", "开发者")
        expectLabel("summary_preferences", preferences)
    }

    @MainActor private func expectValue(_ tag: String, _ value: String) {
        XCTAssertTrue(element(tag).waitForExistence(timeout: 5), "Missing \(tag)")
        if value.isEmpty {
            expect(element(tag), "value == nil OR value == ''")
        } else {
            expect(element(tag), "value == %@", value)
        }
    }

    @MainActor private func expectSelected(_ tag: String, _ selected: Bool) {
        XCTAssertTrue(element(tag).waitForExistence(timeout: 5), "Missing \(tag)")
        // Compose iOS exports ToggleableState/Selected as UIAccessibilityTraitSelected.
        // Query the identifier rather than assuming a native checkbox/switch/radio type.
        expect(element(tag), selected ? "selected == true" : "selected == false")
    }

    @MainActor private func expectEnabled(_ tag: String, _ enabled: Bool) {
        XCTAssertTrue(element(tag).waitForExistence(timeout: 5), "Missing \(tag)")
        expect(element(tag), enabled ? "enabled == true" : "enabled == false")
    }

    @MainActor private func expectAlert(_ tag: String, title: String, description: String) {
        XCTAssertTrue(element(tag).waitForExistence(timeout: 5), "Missing \(tag)")
        expect(element(tag), "label CONTAINS %@ AND label CONTAINS %@", title, description)
    }

    @MainActor private func expectLabel(_ tag: String, _ value: String) {
        XCTAssertTrue(element(tag).waitForExistence(timeout: 5), "Missing \(tag)")
        expect(element(tag), "label == %@", value)
    }

    @MainActor private func expectAbsent(_ tag: String) {
        expect(element(tag), "exists == false")
    }

    @MainActor private func expect(_ target: XCUIElement, _ format: String, _ arguments: CVarArg..., timeout: TimeInterval = 8) {
        let predicate = NSPredicate(format: format, arguments: getVaList(arguments))
        let expectation = XCTNSPredicateExpectation(predicate: predicate, object: target)
        let result = XCTWaiter.wait(for: [expectation], timeout: timeout)
        if result != .completed {
            print("UI assertion expected: \(predicate.predicateFormat)")
            if target.exists {
                print("UI assertion actual: \(target.debugDescription)")
                print("UI assertion state: selected=\(target.isSelected), hittable=\(target.isHittable), value=\(String(describing: target.value))")
            } else {
                print("UI assertion actual: element does not exist")
            }
            print("UI assertion accessibility tree:\n\(app.debugDescription)")
        }
        XCTAssertEqual(result, .completed,
                       "Expected \(predicate.predicateFormat) for \(target)")
    }

    @MainActor private func capture(_ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}

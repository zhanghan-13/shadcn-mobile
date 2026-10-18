package com.shadcn.mobile

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.shadcn.mobile.ui.*
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** End-to-end checks against the Android activity and the shared Compose UI. */
@RunWith(AndroidJUnit4::class)
class WorkspaceFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun imeNextMovesToEmailAndDoneClearsFocusWithoutLosingInput() {
        compose.onNodeWithTag("workspace_name")
            .performScrollTo().performTextInput("设计工作室")
        compose.onNodeWithTag("workspace_name").performImeAction()
        compose.onNodeWithTag("email").assertIsFocused()
            .performTextInput("hello@example.com")
        compose.onNodeWithTag("email").performImeAction()
        compose.onNodeWithTag("email").assertIsNotFocused().assertTextEquals("hello@example.com")
        compose.onNodeWithTag("workspace_name").assertTextEquals("设计工作室")
    }

    @Test
    fun primaryControlsMeetMinimumTouchTargetHeight() {
        compose.onNodeWithTag("theme_toggle").assertHeightIsAtLeast(48.dp)
        listOf("workspace_name", "email", "role_trigger", "terms_checkbox", "submit_button").forEach { tag ->
            compose.onNodeWithTag(tag).performScrollTo().assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    fun emptyFormShowsRequiredErrorsAndDoesNotOpenReview() {
        compose.onNodeWithTag("submit_button").performScrollTo().performClick()

        // The screen uses a scrollable Column; required errors may be off screen.
        listOf("workspace_name_error", "email_error", "error_role", "terms_checkbox_error").forEach { tag ->
            compose.onNodeWithTag(tag, useUnmergedTree = true).assertExists()
        }
        compose.onNodeWithTag("review_dialog").assertDoesNotExist()
        compose.onNodeWithTag("success_panel").assertDoesNotExist()
    }

    @Test
    fun validFormEditsPreferencesCancelsReviewThenCompletes() {
        compose.captureEvidence("01-form")
        compose.enterWorkspaceDetails()
        compose.selectDeveloper(captureScreenshot = true)
        compose.onNodeWithTag("preferences_button").performScrollTo().performClick()
        compose.onNodeWithTag("preferences_sheet").assertIsDisplayed()
        compose.onNodeWithTag("email_updates").assertIsOn()
        compose.onNodeWithTag("product_news").assertIsOff().performClick().assertIsOn()
        compose.captureEvidence("03-sheet", tag = "preferences_sheet")
        compose.onNodeWithTag("save_preferences").performClick()
        compose.onNodeWithTag("preferences_sheet").assertDoesNotExist()

        compose.onNodeWithTag("terms_checkbox").performScrollTo().performClick().assertIsOn()
        compose.onNodeWithTag("submit_button").performScrollTo().performClick()
        compose.onNodeWithTag("review_dialog").assertIsDisplayed()
        compose.captureEvidence("04-review", tag = "review_dialog")
        compose.onNodeWithTag("review_cancel").performClick()
        compose.onNodeWithTag("review_dialog").assertDoesNotExist()
        compose.onNodeWithTag("workspace_name").performScrollTo().assertTextEquals("设计工作室")
        compose.onNodeWithTag("email").performScrollTo().assertTextEquals("hello@example.com")
        compose.onNodeWithTag("terms_checkbox").performScrollTo().assertIsOn()

        // Saved sheet preferences survive leaving the sheet and cancelling review.
        compose.onNodeWithTag("preferences_button").performScrollTo().performClick()
        compose.onNodeWithTag("product_news").assertIsOn()
        compose.onNodeWithTag("save_preferences").performClick()

        compose.onNodeWithTag("submit_button").performScrollTo().performClick()
        compose.onNodeWithTag("review_confirm").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("success_panel").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("review_dialog").assertDoesNotExist()
        compose.onNodeWithTag("success_panel").assertIsDisplayed()
        compose.captureEvidence("05-success", tag = "success_panel")
        compose.onNodeWithTag("create_another").performScrollTo().performClick()
        compose.onNodeWithTag("success_panel").assertDoesNotExist()
        compose.onNodeWithTag("workspace_name").performScrollTo().assert(
            SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")),
        )
        compose.onNodeWithTag("terms_checkbox").performScrollTo().assertIsOff()
    }

    @Test
    fun comboboxRecoversFromNoResultsAndAcceptsSelection() {
        compose.onNodeWithTag("role_trigger").performScrollTo().performClick()
        compose.onNodeWithTag("role_search").performTextInput("astronaut")
        compose.onNodeWithTag("role_empty").assertIsDisplayed()
        compose.onNodeWithTag("role_option_developer").assertDoesNotExist()

        compose.onNodeWithTag("role_search").performTextReplacement("Developer")
        compose.onNodeWithTag("role_empty").assertDoesNotExist()
        // Keep the search keyboard open and send a real touch to catch IME occlusion.
        compose.onNodeWithTag("role_option_developer").assertIsDisplayed().performTouchInput { click() }
        compose.onNodeWithTag("role_search").assertDoesNotExist()

        // Other fields are empty; a valid role must no longer produce its error.
        compose.onNodeWithTag("submit_button").performScrollTo().performClick()
        compose.onNodeWithTag("error_role").assertDoesNotExist()
        compose.onNodeWithTag("workspace_name_error").assertExists()
    }

    @Test
    fun themeTogglePreservesDraftAndRoleSelection() {
        compose.enterWorkspaceDetails()
        compose.selectDeveloper()
        compose.onNodeWithTag("terms_checkbox").performScrollTo().performClick()

        compose.onNodeWithTag("theme_toggle").performClick()
        compose.onNodeWithTag("form_scroll").performSemanticsAction(SemanticsActions.ScrollBy) { scrollBy ->
            scrollBy(0f, -10_000f)
        }
        compose.captureEvidence("06-dark")
        compose.onNodeWithTag("workspace_name").performScrollTo().assertTextEquals("设计工作室")
        compose.onNodeWithTag("email").performScrollTo().assertTextEquals("hello@example.com")
        compose.onNodeWithTag("terms_checkbox").performScrollTo().assertIsOn()
        compose.onNodeWithTag("submit_button").performScrollTo().performClick()
        compose.onNodeWithTag("review_dialog").assertIsDisplayed()
    }

    @Test
    fun systemBackClosesEachOverlayWithoutLosingDraft() {
        compose.enterWorkspaceDetails()
        compose.onNodeWithTag("role_trigger").performScrollTo().performClick()
        compose.onNodeWithTag("role_search").assertExists()
        closeSoftKeyboard()
        pressBack()
        compose.onNodeWithTag("role_search").assertDoesNotExist()

        compose.onNodeWithTag("preferences_button").performScrollTo().performClick()
        compose.onNodeWithTag("preferences_sheet").assertIsDisplayed()
        compose.onNodeWithTag("product_news").performClick().assertIsOn()
        pressBack()
        compose.onNodeWithTag("preferences_sheet").assertDoesNotExist()
        compose.onNodeWithTag("preferences_button").performScrollTo().performClick()
        compose.onNodeWithTag("product_news").assertIsOff()
        pressBack()
        compose.onNodeWithTag("preferences_sheet").assertDoesNotExist()

        compose.selectDeveloper()
        compose.onNodeWithTag("terms_checkbox").performScrollTo().performClick()
        compose.onNodeWithTag("submit_button").performScrollTo().performClick()
        compose.onNodeWithTag("review_dialog").assertIsDisplayed()
        pressBack()
        compose.onNodeWithTag("review_dialog").assertDoesNotExist()
        compose.onNodeWithTag("workspace_name").performScrollTo().assertTextEquals("设计工作室")
        compose.onNodeWithTag("email").performScrollTo().assertTextEquals("hello@example.com")
    }

    @Test
    fun switchLabelAndTrackToggleOnceWhileDisabledSwitchStaysOn() {
        openComponent("switch")
        val toggle = compose.onNodeWithTag("switch_updates").assertIsOff()
            .assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
        toggle.performTouchInput { click(Offset(width * 0.2f, height / 2f)) }.assertIsOn()
        compose.onNodeWithTag("switch_count").assertTextEquals("已切换 1 次")
        toggle.performTouchInput { click(Offset(width * 0.94f, height / 2f)) }.assertIsOff()
        compose.onNodeWithTag("switch_count").assertTextEquals("已切换 2 次")
        compose.onNodeWithTag("switch_disabled").assertIsNotEnabled().assertIsOn()
            .performTouchInput { click() }.assertIsOn()
        compose.onNodeWithTag("switch_disabled").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithTag("switch_count").assertTextEquals("已切换 2 次")
        compose.captureEvidence("07-switch")
    }

    @Test
    fun textareaPreservesChineseNewlinesAndReadOnlyContent() {
        openComponent("textarea")
        compose.onNodeWithTag("textarea_validate").performScrollTo().performClick()
        compose.onNodeWithTag("textarea_notes_error").assertExists()
        val input = "设计说明\n  保留两处空格\n下一步计划"
        compose.onNodeWithTag("textarea_notes").performScrollTo().performTextInput(input)
        compose.onNodeWithTag("textarea_notes").assertTextEquals(input)
        compose.onNodeWithTag("textarea_notes_error").assertDoesNotExist()
        closeSoftKeyboard()
        compose.onNodeWithTag("textarea_readonly").performScrollTo().assertIsEnabled()
            .performTouchInput { click() }
            .assertTextEquals("这段说明可以选择和复制。\n只读内容不会被编辑。")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))
        compose.onNodeWithTag("textarea_disabled").performScrollTo().assertIsNotEnabled()
            .performTouchInput { click() }.assertTextEquals("当前不可编辑")
        compose.onNodeWithTag("textarea_notes").performScrollTo().assertTextEquals(input)
        compose.captureEvidence("08-textarea")
    }

    @Test
    fun radioGroupMaintainsOneSelectionAndIgnoresDisabledOption() {
        openComponent("radio-group")
        compose.onNodeWithTag("radio_plan_free").assertIsSelected()
            .assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
        compose.onNodeWithTag("radio_plan_pro").assertIsNotSelected().performTouchInput { click() }
            .assertIsSelected()
        compose.onNodeWithTag("radio_plan_free").assertIsNotSelected()
        compose.onNodeWithTag("radio_selected").assertTextEquals("当前方案：专业版")
        compose.onNodeWithTag("radio_plan_disabled").assertIsNotEnabled()
            .performTouchInput { click() }.assertIsNotSelected()
        compose.onNodeWithTag("radio_plan_disabled").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithTag("radio_plan_pro").assertIsSelected()
        compose.onNodeWithTag("radio_count").assertTextEquals("已切换 1 次")
        compose.onNodeWithTag("radio_plan_free").performTouchInput { click() }.assertIsSelected()
        compose.onNodeWithTag("radio_plan_pro").assertIsNotSelected()
        compose.onNodeWithTag("radio_count").assertTextEquals("已切换 2 次")
        compose.captureEvidence("09-radio")
    }

    @Test
    fun alertAnnouncesDynamicFeedbackWithoutActingLikeAButton() {
        openComponent("alert")
        compose.onNodeWithTag("alert_default").assertTextContains("本地演示")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
        compose.onNodeWithTag("alert_destructive").assertTextContains("无法保存更改")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.captureEvidence("10-alert")
        compose.onNodeWithTag("alert_trigger").performTouchInput { click() }.assertIsNotEnabled()
        compose.onNodeWithTag("alert_destructive").assertTextContains("已重新连接")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    @Test
    fun tabsKeepDraftAcrossPanelsAndIgnoreDisabledSelection() {
        openComponent("tabs")
        compose.onNodeWithTag("gallery_tabs_profile").assertIsSelected()
            .assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
        compose.onNodeWithTag("tabs_draft").performTextInput("设计师的草稿")
        compose.onNodeWithTag("tabs_draft").performImeAction()
        compose.onNodeWithTag("gallery_tabs_activity").performTouchInput { click() }.assertIsSelected()
        compose.onNodeWithTag("gallery_tabs_profile").assertIsNotSelected()
        compose.onNodeWithTag("tabs_draft").assertDoesNotExist()
        compose.onNodeWithTag("tabs_activity").assertTextEquals("最近活动：已创建工作空间")
        compose.onNodeWithTag("gallery_tabs_disabled").performScrollTo().assertIsNotEnabled()
            .performTouchInput { click() }
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithTag("tabs_count").assertTextEquals("已切换 1 次")
        compose.onNodeWithTag("gallery_tabs_profile").performScrollTo().performTouchInput { click() }.assertIsSelected()
        compose.onNodeWithTag("tabs_draft").assertTextEquals("设计师的草稿")
        compose.onNodeWithTag("tabs_activity").assertDoesNotExist()
        compose.onNodeWithTag("tabs_count").assertTextEquals("已切换 2 次")
        compose.captureEvidence("11-tabs")
    }

    @Test
    fun accordionExposesOnlyOpenContentAndIgnoresDisabledActivation() {
        openComponent("accordion")
        compose.onNodeWithTag("gallery_accordion_native_content").assertDoesNotExist()
        compose.onNodeWithTag("gallery_accordion_native").assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "已折叠"))
            .performTouchInput { click() }
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "已展开"))
        compose.onNodeWithTag("gallery_accordion_native_content").assertIsDisplayed()
        compose.onNodeWithTag("gallery_accordion_ownership").performTouchInput { click() }
        compose.onNodeWithTag("gallery_accordion_native_content").assertDoesNotExist()
        compose.onNodeWithTag("gallery_accordion_ownership_content").assertIsDisplayed()
        compose.onNodeWithTag("gallery_accordion_disabled").assertIsNotEnabled()
            .performTouchInput { click() }
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithTag("accordion_count").assertTextEquals("已切换 2 次")
        compose.captureEvidence("12-accordion")
        compose.onNodeWithTag("gallery_accordion_ownership").performTouchInput { click() }
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "已折叠"))
        compose.onNodeWithTag("gallery_accordion_ownership_content").assertDoesNotExist()
        compose.onNodeWithTag("accordion_count").assertTextEquals("已切换 3 次")
    }

    @Test
    fun sliderDragAndAccessibilityAdjustValueWhileDisabledSliderStaysFixed() {
        openComponent("slider")
        val slider = compose.onNodeWithTag("gallery_slider").assertHeightIsAtLeast(48.dp)
        slider.assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo(40f, 0f..100f, 9)))
        slider.performTouchInput { swipe(Offset(width * 0.4f, height / 2f), Offset(width * 0.85f, height / 2f), 450) }
        slider.assert(SemanticsMatcher("drag increases and snaps to a ten-point step") {
            val value = it.config[SemanticsProperties.ProgressBarRangeInfo].current
            value >= 70f && value <= 100f && value.toInt() % 10 == 0
        })
        compose.onNodeWithTag("gallery_slider_finished").assertTextEquals("已完成 1 次调整")
        slider.performSemanticsAction(SemanticsActions.SetProgress) { it(20f) }
        compose.onNodeWithTag("gallery_slider_value").assertTextEquals("音量：20")
        val changesBefore = compose.onNodeWithTag("gallery_slider_count").fetchSemanticsNode().config[SemanticsProperties.Text]
        val finishesBefore = compose.onNodeWithTag("gallery_slider_finished").fetchSemanticsNode().config[SemanticsProperties.Text]
        val disabledBefore = compose.onNodeWithTag("gallery_slider_disabled").fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
        org.junit.Assert.assertEquals(60f, disabledBefore.current, 0.001f)
        compose.onNodeWithTag("gallery_slider_disabled").assertIsNotEnabled()
            .performTouchInput { swipe(Offset(width * 0.6f, height / 2f), Offset(width * 0.9f, height / 2f), 350) }
            .performSemanticsAction(SemanticsActions.SetProgress) { it(90f) }
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
                disabledBefore))
        compose.onNodeWithTag("gallery_slider_count").assert(SemanticsMatcher.expectValue(SemanticsProperties.Text, changesBefore))
        compose.onNodeWithTag("gallery_slider_finished").assert(SemanticsMatcher.expectValue(SemanticsProperties.Text, finishesBefore))
        compose.captureEvidence("13-slider")
    }

    @Test
    fun progressReportsRealValuesAndUnknownStateWithoutClickActions() {
        openComponent("progress")
        val progress = compose.onNodeWithTag("gallery_progress")
        progress.assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo(0.25f, 0f..1f)))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetProgress))
        compose.onNodeWithTag("gallery_progress_indeterminate").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "进度未定"))
        compose.onNodeWithTag("progress_advance").performTouchInput { click() }
        compose.onNodeWithTag("gallery_progress_value").assertTextEquals("进度：50%")
        progress.assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo(0.5f, 0f..1f)))
        compose.onNodeWithTag("progress_advance").performTouchInput { click() }
        compose.onNodeWithTag("progress_advance").performTouchInput { click() }.assertIsNotEnabled()
        compose.onNodeWithTag("gallery_progress_value").assertTextEquals("进度：100%")
        compose.captureEvidence("14-progress")
        compose.onNodeWithTag("progress_reset").performTouchInput { click() }
        compose.onNodeWithTag("gallery_progress_value").assertTextEquals("进度：0%")
        compose.onNodeWithTag("progress_advance").assertIsEnabled()
        progress.assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo(0f, 0f..1f)))
    }

    @Test
    fun galleryBackNavigationPreservesFormThemeAndComponentState() {
        compose.enterWorkspaceDetails()
        compose.selectDeveloper()
        compose.onNodeWithTag("terms_checkbox").performScrollTo().performClick()
        compose.onNodeWithTag("theme_toggle").performClick()
        openComponent("switch")
        compose.onNodeWithTag("switch_updates").performClick().assertIsOn()
        pressBack()
        compose.onNodeWithTag("catalog_switch").performScrollTo().performClick()
        compose.onNodeWithTag("switch_updates").assertIsOn()
        compose.onNodeWithTag("switch_count").assertTextEquals("已切换 1 次")
        compose.onNodeWithTag("component_back").performClick()
        pressBack()
        compose.onNodeWithTag("workspace_name").performScrollTo().assertTextEquals("设计工作室")
        compose.onNodeWithTag("email").performScrollTo().assertTextEquals("hello@example.com")
        compose.onNodeWithTag("terms_checkbox").performScrollTo().assertIsOn()
        compose.onNodeWithTag("theme_toggle").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("切换到浅色主题")))
        compose.onNodeWithTag("submit_button").performScrollTo().performClick()
        compose.onNodeWithTag("review_dialog").assertIsDisplayed()
    }

    @Test
    fun activityRecreationRestoresGalleryRouteTextAndHiddenFormDraft() {
        compose.enterWorkspaceDetails()
        openComponent("textarea")
        compose.onNodeWithTag("textarea_notes").performScrollTo().performTextInput("保留草稿\n第二行")
        closeSoftKeyboard()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("textarea_notes").performScrollTo().assertTextEquals("保留草稿\n第二行")
        pressBack()
        pressBack()
        compose.onNodeWithTag("workspace_name").performScrollTo().assertTextEquals("设计工作室")
        compose.onNodeWithTag("email").performScrollTo().assertTextEquals("hello@example.com")
    }

    private fun openComponent(name: String) {
        compose.onNodeWithTag("open_components").performClick()
        compose.onNodeWithTag("catalog_$name").performScrollTo().performClick()
        compose.onNodeWithTag("component_scroll").assertIsDisplayed()
    }
}

/** Isolated rule avoids setting content twice in the real activity. */
@RunWith(AndroidJUnit4::class)
class WorkspaceStateRestorationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun tabsKeyboardNavigationSkipsDisabledItemsInRtlAndPreservesPanelState() {
        val selected = mutableStateOf<String?>("first")
        lateinit var inputModeManager: InputModeManager
        compose.setContent {
            ShadcnTheme {
                inputModeManager = LocalInputModeManager.current
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    STabs(listOf(TabOption("first", "第一项"), TabOption("disabled", "禁用", false),
                        TabOption("last", "最后一项")), selected.value, { selected.value = it }, testTag = "rtl_tabs") { id ->
                        val draft = rememberSaveable { mutableStateOf("") }
                        SInput(draft.value, { draft.value = it }, "面板草稿", testTag = "draft_$id")
                    }
                }
            }
        }
        compose.onNodeWithTag("draft_first").performTextInput("保留在面板内")
        closeSoftKeyboard()
        // Buttons intentionally do not take keyboard focus while Android is in touch mode.
        compose.runOnIdle { inputModeManager.requestInputMode(InputMode.Keyboard) }
        compose.onNodeWithTag("rtl_tabs_first").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            .assertIsFocused().performKeyInput { pressKey(Key.DirectionLeft) }
        compose.onNodeWithTag("rtl_tabs_last").assertIsSelected().assertIsFocused()
        compose.onNodeWithTag("draft_first").assertDoesNotExist()
        compose.onNodeWithTag("rtl_tabs_last").performKeyInput { pressKey(Key.DirectionRight) }
        compose.onNodeWithTag("rtl_tabs_first").assertIsSelected().assertIsFocused()
        compose.onNodeWithTag("draft_first").assertTextEquals("保留在面板内")
        compose.onNodeWithTag("rtl_tabs_first").performKeyInput { pressKey(Key.MoveEnd) }
        compose.onNodeWithTag("rtl_tabs_last").assertIsSelected()
        compose.onNodeWithTag("rtl_tabs_last").performKeyInput { pressKey(Key.MoveHome) }
        compose.onNodeWithTag("rtl_tabs_first").assertIsSelected()
    }

    @Test
    fun sliderAccessibilitySnapsNegativeRangesAndBlocksDisabledCallbacks() {
        val value = mutableStateOf(-10f)
        val enabled = mutableStateOf(true)
        val changes = mutableListOf<Float>()
        var finishes = 0
        compose.setContent {
            ShadcnTheme {
                SSlider(value.value, { value.value = it; changes += it }, "增益",
                    valueRange = -10f..0f, steps = 1, enabled = enabled.value,
                    onValueChangeFinished = { finishes++ }, testTag = "negative_slider")
            }
        }
        compose.onNodeWithTag("negative_slider").performSemanticsAction(SemanticsActions.SetProgress) { it(-3f) }
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(-5f, -10f..0f, 1)))
        compose.runOnIdle {
            org.junit.Assert.assertEquals(listOf(-5f), changes)
            org.junit.Assert.assertEquals(1, finishes)
        }
        compose.onNodeWithTag("negative_slider").performSemanticsAction(SemanticsActions.SetProgress) { it(100f) }
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(0f, -10f..0f, 1)))
        compose.runOnIdle {
            org.junit.Assert.assertEquals(listOf(-5f, 0f), changes)
            org.junit.Assert.assertEquals(2, finishes)
            enabled.value = false
        }
        compose.onNodeWithTag("negative_slider").assertIsNotEnabled()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(-10f) }
        compose.runOnIdle {
            org.junit.Assert.assertEquals(listOf(-5f, 0f), changes)
            org.junit.Assert.assertEquals(2, finishes)
        }
    }

    @Test
    fun disabledAndLoadingPrimitivesIgnoreAccessibilityActivation() {
        val dispatched = mutableListOf<String>()
        compose.setContent {
            ShadcnTheme {
                Column {
                    SButton("禁用按钮", { dispatched += "disabled" }, Modifier.testTag("disabled_button"), enabled = false)
                    SButton("正在保存", { dispatched += "loading" }, Modifier.testTag("loading_button"), loading = true)
                    SCheckbox(false, { dispatched += "checkbox" }, "禁用复选框", enabled = false, testTag = "disabled_checkbox")
                }
            }
        }
        // Accessibility bridges can invoke the action even with Disabled semantics.
        // The component must still protect the caller's side effects.
        listOf("disabled_button", "loading_button", "disabled_checkbox").forEach { tag ->
            compose.onNodeWithTag(tag).assertIsNotEnabled()
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
        }
        compose.runOnIdle { org.junit.Assert.assertEquals(emptyList<String>(), dispatched) }
    }

    @Test
    fun savedInstanceStateRestoresFormAndNotificationPreferences() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { App() }
        compose.enterWorkspaceDetails()
        compose.selectDeveloper()
        compose.onNodeWithTag("terms_checkbox").performScrollTo().performClick()
        compose.onNodeWithTag("preferences_button").performScrollTo().performClick()
        compose.onNodeWithTag("product_news").performClick()
        compose.onNodeWithTag("save_preferences").performClick()

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithTag("workspace_name").performScrollTo().assertTextEquals("设计工作室")
        compose.onNodeWithTag("email").performScrollTo().assertTextEquals("hello@example.com")
        compose.onNodeWithTag("terms_checkbox").performScrollTo().assertIsOn()
        compose.onNodeWithTag("preferences_button").performScrollTo().performClick()
        compose.onNodeWithTag("email_updates").assertIsOn()
        compose.onNodeWithTag("product_news").assertIsOn()
        compose.onNodeWithTag("save_preferences").performClick()
        compose.onNodeWithTag("submit_button").performScrollTo().performClick()
        compose.onNodeWithTag("review_dialog").assertIsDisplayed()
    }
}

private fun ComposeContentTestRule.enterWorkspaceDetails() {
    onNodeWithTag("workspace_name").performScrollTo().performTextInput("设计工作室")
    onNodeWithTag("email").performScrollTo().performTextInput("hello@example.com")
    closeSoftKeyboard()
}

private fun ComposeContentTestRule.selectDeveloper(captureScreenshot: Boolean = false) {
    onNodeWithTag("role_trigger").performScrollTo().performClick()
    if (captureScreenshot) captureEvidence("02-combobox", tag = "role_menu")
    onNodeWithTag("role_search").performTextInput("Developer")
    closeSoftKeyboard()
    onNodeWithTag("role_option_developer").performClick()
}

/** Manual review evidence only; no screenshot pixels are used as test assertions. */
private fun ComposeContentTestRule.captureEvidence(name: String, tag: String? = null) {
    waitForIdle()
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val externalFiles = requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)) {
        "External app files directory is unavailable for screenshot evidence"
    }
    val directory = File(externalFiles, "screenshots")
    check(directory.isDirectory || directory.mkdirs()) { "Cannot create $directory" }
    val node = if (tag == null) onRoot() else onNodeWithTag(tag)
    val screenshot = node.captureToImage().asAndroidBitmap()
    try {
        File(directory, "$name.png").outputStream().use { output ->
            check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write screenshot $name"
            }
        }
    } finally {
        screenshot.recycle()
    }
}

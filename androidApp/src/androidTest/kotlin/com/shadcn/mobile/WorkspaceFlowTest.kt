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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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
}

/** Isolated rule avoids setting content twice in the real activity. */
@RunWith(AndroidJUnit4::class)
class WorkspaceStateRestorationTest {
    @get:Rule
    val compose = createComposeRule()

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
    closeSoftKeyboard()
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

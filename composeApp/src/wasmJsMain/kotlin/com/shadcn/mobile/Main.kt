@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.shadcn.mobile

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import com.shadcn.mobile.web.resources.Res
import com.shadcn.mobile.web.resources.noto_sans_sc_regular
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

private fun queryParameter(key: String): String = js("new URLSearchParams(window.location.search).get(key) || ''")

private fun signalReady(): Unit = js("{ window.dispatchEvent(new Event('shadcn-ready')); }")

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class, ExperimentalTextApi::class)
fun main() {
    val component = queryParameter("component").lowercase()
    val initialDarkTheme = when (queryParameter("theme")) {
        "dark" -> true
        "light" -> false
        else -> null
    }
    ComposeViewport(viewportContainerId = "app") {
        // The bundled SIL-licensed font makes CJK work without third-party font services.
        val font by preloadFont(Res.font.noto_sans_sc_regular)
        val resolver = LocalFontFamilyResolver.current
        var fontsReady by remember { mutableStateOf(false) }
        LaunchedEffect(font) {
            font?.let {
                resolver.preload(FontFamily(it))
                fontsReady = true
            }
        }
        if (fontsReady) {
            SideEffect { signalReady() }
            if (component in componentNames) {
                ComponentGallery(component, initialDarkTheme)
            } else {
                App(initialDarkTheme = initialDarkTheme)
            }
        }
    }
}

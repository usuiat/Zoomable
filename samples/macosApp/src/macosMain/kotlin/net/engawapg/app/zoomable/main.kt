package net.engawapg.app.zoomable

import androidx.compose.ui.window.Window
import platform.AppKit.NSApplication

fun main() {
    NSApplication.sharedApplication()
    Window(title = "Zoomable") {
        App()
    }
    NSApplication.sharedApplication().run()
}

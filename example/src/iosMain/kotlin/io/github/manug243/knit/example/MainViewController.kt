package io.github.manug243.knit.example

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // The CI starts the app with this argument to screenshot a screen that has a top bar.
    val openDetail = NSProcessInfo.processInfo.arguments.contains("-knit-detail")
    return ComposeUIViewController { App(openDetailOnStart = openDetail) }
}

package io.github.manug243.knit.example

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    App(bottomBar = { SpikeTabBar() })
}

package io.github.manug243.knit.example

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.darwin.NSObject

private val SpikeTabs = listOf("Home" to "house", "Lists" to "list.bullet", "Settings" to "gearshape")

/**
 * Spike: a stock UITabBar as a native overlay above the Compose canvas. Answers one question:
 * does it render as Liquid Glass over Compose content when built with the iOS 26 SDK?
 * The fixed height is intentional here; phase 1 measures it at runtime.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun BoxScope.SpikeTabBar() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    // UITabBar references its delegate weakly, so the composition has to keep it alive.
    val delegate = remember { SpikeTabBarDelegate { selectedIndex = it } }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    UIKitView(
        factory = {
            UITabBar().apply {
                setItems(
                    SpikeTabs.mapIndexed { index, (title, symbol) ->
                        UITabBarItem(title = title, image = UIImage.systemImageNamed(symbol), tag = index.toLong())
                    },
                    animated = false,
                )
                this.delegate = delegate
            }
        },
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(49.dp + bottomInset),
        update = { bar ->
            val item = bar.items?.getOrNull(selectedIndex) as? UITabBarItem
            if (item != null && bar.selectedItem != item) bar.selectedItem = item
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
            placedAsOverlay = true,
        ),
    )
}

private class SpikeTabBarDelegate(
    private val onSelect: (Int) -> Unit,
) : NSObject(), UITabBarDelegateProtocol {
    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        onSelect(didSelectItem.tag.toInt())
    }
}

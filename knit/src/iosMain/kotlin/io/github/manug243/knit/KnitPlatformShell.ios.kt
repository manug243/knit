package io.github.manug243.knit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIImage
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.darwin.NSObject

/** Height of a classic tab bar without the home indicator region. */
private val ClassicTabBarHeight = 49.dp

/**
 * iOS chrome: the content fills the screen in one Compose tree and a stock [UITabBar] sits above it
 * as a native overlay. Built with the iOS 26 SDK the bar renders as Liquid Glass.
 *
 * The bar stays mounted while hidden and slides out of the screen, because a `UIKitView` is torn down
 * as soon as it leaves the composition.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun KnitPlatformShell(
    items: List<ShellItem>,
    selectedIndex: Int,
    barVisible: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // The bar's own measurement can only make the area larger, never smaller than the classic size.
    var measuredHeight by remember { mutableStateOf(0.dp) }
    val barHeight = maxOf(ClassicTabBarHeight + bottomInset, measuredHeight)

    val coveredHeight by animateDpAsState(if (barVisible) barHeight else 0.dp)
    val slideOut by animateDpAsState(if (barVisible) 0.dp else barHeight)

    val currentOnSelect by rememberUpdatedState(onSelect)
    // UITabBar references its delegate weakly, so the composition has to keep it alive.
    // UIKit calls it on user taps only, not when selectedItem is set from code, so there is no loop.
    val delegate = remember { KnitTabBarDelegate { index -> currentOnSelect(index) } }
    val synchronizer = remember { TabBarSynchronizer() }

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            ProvideKnitContentPadding(PaddingValues(bottom = coveredHeight), content)
        }
        UIKitView(
            factory = { UITabBar().apply { this.delegate = delegate } },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(barHeight)
                .offset { IntOffset(x = 0, y = slideOut.roundToPx()) },
            update = { bar ->
                synchronizer.sync(bar, items, selectedIndex)
                val height = bar.sizeThatFits(CGSizeMake(0.0, 0.0)).useContents { height }.dp
                if (height != measuredHeight) measuredHeight = height
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = true,
                placedAsOverlay = true,
            ),
        )
    }
}

/** Keeps the native items and the selection in line with the Compose state. */
@OptIn(ExperimentalForeignApi::class)
private class TabBarSynchronizer {
    private var signature: List<Pair<String, String>>? = null

    fun sync(bar: UITabBar, items: List<ShellItem>, selectedIndex: Int) {
        val newSignature = items.map { it.title to it.icon.sfSymbol }
        if (newSignature != signature) {
            bar.setItems(
                items.mapIndexed { index, item ->
                    UITabBarItem(
                        title = item.title,
                        image = UIImage.systemImageNamed(item.icon.sfSymbol),
                        tag = index.toLong(),
                    )
                },
                animated = false,
            )
            signature = newSignature
        }
        val target = bar.items?.getOrNull(selectedIndex) as? UITabBarItem
        if (target != null && bar.selectedItem != target) {
            bar.selectedItem = target
        }
    }
}

private class KnitTabBarDelegate(
    private val onSelect: (Int) -> Unit,
) : NSObject(), UITabBarDelegateProtocol {
    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        onSelect(didSelectItem.tag.toInt())
    }
}

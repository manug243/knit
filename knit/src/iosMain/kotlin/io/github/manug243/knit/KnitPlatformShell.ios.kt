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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIImage
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarDelegateProtocol
import platform.UIKit.UINavigationItem
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UIView
import platform.darwin.NSObject

/** Height of a classic tab bar without the home indicator region. */
private val ClassicTabBarHeight = 49.dp

/** Height of a classic navigation bar without the status bar region. */
private val ClassicNavigationBarHeight = 44.dp

/**
 * iOS chrome: the content fills the screen in one Compose tree and a stock [UITabBar] sits above it
 * as a native overlay. Built with the iOS 26 SDK the bar renders as Liquid Glass.
 *
 * The bars stay mounted while hidden and fade out, because a `UIKitView` is torn down as soon as it
 * leaves the composition. The navigation bar below the status bar stays in place and only swaps its
 * items, like in a navigation controller.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun KnitPlatformShell(
    items: List<ShellItem>,
    selectedIndex: Int,
    barVisible: Boolean,
    onSelect: (Int) -> Unit,
    topBar: ShellTopBar?,
    // Colors and custom bars only apply to the Compose bars of the other platforms. The native bars
    // keep their system look.
    @Suppress("UNUSED_PARAMETER") style: ShellStyle,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // The bar's own measurement can only make the area larger, never smaller than the classic size.
    var measuredHeight by remember { mutableStateOf(0.dp) }
    val barHeight = maxOf(ClassicTabBarHeight + bottomInset, measuredHeight)

    val coveredHeight by animateDpAsState(if (barVisible) barHeight else 0.dp)

    // Without a navigation controller the bar knows nothing about the status bar and lays its content
    // out at the top of its own frame. So the frame is exactly the 44 dp of content and sits below
    // the status bar. It never moves: like in iOS, only the items change when a screen is pushed, and
    // the whole bar fades when a screen has none.
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = ClassicNavigationBarHeight
    val shownTopBar = remember { LastValue<ShellTopBar>() }
    if (topBar != null) shownTopBar.value = topBar
    val coveredTop by animateDpAsState(if (topBar != null) ClassicNavigationBarHeight else 0.dp)
    // Same as the tab bar delegate: UINavigationBar holds it weakly.
    val navDelegate = remember { KnitNavigationBarDelegate { shownTopBar.value?.onBack?.invoke() } }
    val navSynchronizer = remember { NavigationBarSynchronizer() }

    val currentOnSelect by rememberUpdatedState(onSelect)
    // UITabBar references its delegate weakly, so the composition has to keep it alive.
    // UIKit calls it on user taps only, not when selectedItem is set from code, so there is no loop.
    val delegate = remember { KnitTabBarDelegate { index -> currentOnSelect(index) } }
    val synchronizer = remember { TabBarSynchronizer() }

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            ProvideKnitContentPadding(PaddingValues(top = coveredTop, bottom = coveredHeight), content)
        }
        UIKitView(
            factory = {
                UINavigationBar().apply {
                    this.delegate = navDelegate
                    alpha = 0.0
                    userInteractionEnabled = false
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = statusBarInset)
                .fillMaxWidth()
                .height(navBarHeight),
            // The lambda has to capture topBar: a new instance on every change is what makes UIKitView run
            // update again. Reading only shownTopBar, a plain field, left the bar empty when the screen
            // was opened after the first frame.
            update = { bar ->
                (topBar ?: shownTopBar.value)?.let { navSynchronizer.sync(bar, it) }
                bar.fadeTo(topBar != null)
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = true,
                placedAsOverlay = true,
            ),
        )
        UIKitView(
            factory = {
                UITabBar().apply {
                    this.delegate = delegate
                    alpha = if (barVisible) 1.0 else 0.0
                    userInteractionEnabled = barVisible
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(barHeight),
            update = { bar ->
                synchronizer.sync(bar, items, selectedIndex)
                val height = bar.sizeThatFits(CGSizeMake(0.0, 0.0)).useContents { height }.dp
                if (height != measuredHeight) measuredHeight = height
                bar.fadeTo(barVisible)
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = true,
                placedAsOverlay = true,
            ),
        )
    }
}

private const val BarFadeSeconds = 0.25

/**
 * Fades a native bar in or out and switches its touches off while it is invisible. A hidden bar
 * stays in place, so nothing slides across the screen like with an offset.
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIView.fadeTo(visible: Boolean) {
    val target = if (visible) 1.0 else 0.0
    userInteractionEnabled = visible
    if (alpha != target) {
        UIView.animateWithDuration(BarFadeSeconds) { alpha = target }
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

/**
 * Keeps the navigation items in line with the Compose state. A bar with two items shows the system
 * back button, whose label is the title of the first one.
 */
@OptIn(ExperimentalForeignApi::class)
private class NavigationBarSynchronizer {
    private var signature: Triple<String, String?, List<Pair<String, String>>>? = null

    // The button handlers read the actions from here, so they always call the latest callbacks
    // although the native buttons are only rebuilt when title or icon change.
    private var actions: List<KnitAction> = emptyList()

    fun sync(bar: UINavigationBar, topBar: ShellTopBar) {
        actions = topBar.actions
        val newSignature = Triple(
            topBar.title,
            topBar.backTitle,
            topBar.actions.map { it.label to it.icon.sfSymbol },
        )
        if (newSignature == signature) return
        val current = UINavigationItem(title = topBar.title)
        // UIKit lists trailing buttons from the outermost inwards, like KnitTopBar.actions.
        current.rightBarButtonItems = topBar.actions.mapIndexed { index, action ->
            UIBarButtonItem(
                primaryAction = UIAction.actionWithTitle(
                    title = action.label,
                    image = UIImage.systemImageNamed(action.icon.sfSymbol),
                    identifier = null,
                    handler = { _ -> actions.getOrNull(index)?.onClick?.invoke() },
                ),
            )
        }
        val items = buildList {
            topBar.backTitle?.let { add(UINavigationItem(title = it)) }
            add(current)
        }
        // Items change with the native push or pop animation while the bar is on screen. While it is
        // hidden or fading in they are set at once.
        val animated = signature != null && bar.alpha > 0.5
        bar.setItems(items, animated = animated)
        signature = newSignature
    }
}

/**
 * Receives taps on the system back button. The pop is refused on purpose: Navigation 3 owns the
 * back stack, and the bar follows once the stack has changed.
 */
private class KnitNavigationBarDelegate(
    private val onBack: () -> Unit,
) : NSObject(), UINavigationBarDelegateProtocol {
    override fun navigationBar(navigationBar: UINavigationBar, shouldPopItem: UINavigationItem): Boolean {
        onBack()
        return false
    }
}

private class KnitTabBarDelegate(
    private val onSelect: (Int) -> Unit,
) : NSObject(), UITabBarDelegateProtocol {
    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        onSelect(didSelectItem.tag.toInt())
    }
}

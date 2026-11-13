package io.github.manug243.knit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIAction
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarDelegateProtocol
import platform.UIKit.UINavigationItem
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UIUserInterfaceStyle
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
 *
 * Both bars keep their full size at all times. A hidden bar gives up its touch area by being moved
 * off the screen once its fade has finished (see [BarFader]), never by shrinking. Changing the size
 * made UIKit lay the tab bar out again from a zero height whenever it came back, which is the likely
 * cause of a selection pill that slid in and of titles that went missing.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun KnitPlatformShell(
    items: List<ShellItem>,
    selectedIndex: Int,
    barVisible: Boolean,
    barAllowed: Boolean,
    onSelect: (Int) -> Unit,
    topBar: ShellTopBar?,
    // Only the accent applies. The other colors and the custom bars are for the Compose bars of the
    // other platforms, the native bars keep their system look.
    style: ShellStyle,
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
    val navFader = remember { BarFader(parked = topBar == null) }

    val currentOnSelect by rememberUpdatedState(onSelect)
    // UITabBar references its delegate weakly, so the composition has to keep it alive.
    // UIKit calls it on user taps only, not when selectedItem is set from code, so there is no loop.
    val delegate = remember { KnitTabBarDelegate { index -> currentOnSelect(index) } }
    val synchronizer = remember { TabBarSynchronizer() }
    val tabFader = remember { BarFader(parked = !barVisible) }
    // The bar gets its light or dark style from Compose instead of resolving it on its own, and is
    // rebuilt when the app returns to the foreground. Without that the glass bar could come back from
    // the background in the light style while the app is dark, until a tab was switched.
    val dark = isSystemInDarkTheme()
    val tint = style.colors.accent.toUIColorOrNull()
    val tinter = remember { BarTinter() }
    val navTinter = remember { BarTinter() }
    val foregroundCount = rememberForegroundCount()

    val system = systemSafePadding()
    val layoutDirection = LocalLayoutDirection.current
    // A hidden bar gives up its area. The wrapper view that Compose puts around an interop view takes
    // every touch inside its bounds, even when the bar itself ignores touches: in CMP 1.12 the overlay
    // input view asks Compose which interop view is under the finger and hit-tests that view's wrapper,
    // which then answers with itself. A Compose button below an invisible bar would be dead. The area
    // is freed by moving the bar off the screen, not by shrinking it, so its layout never changes.
    // A visible bar counts as on screen at once, so the move back lands in the same frame as the start
    // of its fade-in.
    val navBarOnScreen = topBar != null || !navFader.parked
    val tabBarOnScreen = barVisible || !tabFader.parked

    // What a start screen of a tab keeps clear at the bottom. Navigation does not change it, so the
    // screen below does not move while a detail screen slides in.
    val rootBarHeight by animateDpAsState(if (barAllowed) barHeight else 0.dp)
    val shellPadding = ShellPadding { hasTopBar, isTabRoot ->
        val top = if (hasTopBar) ClassicNavigationBarHeight else 0.dp
        val bottom = if (isTabRoot) rootBarHeight else 0.dp
        EntryPaddingValues(
            chrome = PaddingValues(top = top, bottom = bottom),
            // The height of the tab bar already contains the home indicator region, so the larger one counts.
            safe = PaddingValues(
                start = system.calculateStartPadding(layoutDirection),
                top = system.calculateTopPadding() + top,
                end = system.calculateEndPadding(layoutDirection),
                bottom = maxOf(system.calculateBottomPadding(), bottom),
            ),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fallback for content outside a screen. Screens get their own values from shellPadding.
            val safe = PaddingValues(
                start = system.calculateStartPadding(layoutDirection),
                top = system.calculateTopPadding() + coveredTop,
                end = system.calculateEndPadding(layoutDirection),
                bottom = maxOf(system.calculateBottomPadding(), coveredHeight),
            )
            CompositionLocalProvider(LocalShellPadding provides shellPadding) {
                ProvideKnitContentPadding(
                    chrome = PaddingValues(top = coveredTop, bottom = coveredHeight),
                    safe = safe,
                    content = content,
                )
            }
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
                // Parked above the top edge. Only the position changes: CMP sets interop frames inside
                // performWithoutAnimation, and moving the wrapper does not lay the bar out again.
                .offset {
                    IntOffset(0, if (navBarOnScreen) 0 else -(statusBarInset + navBarHeight).roundToPx())
                }
                .padding(top = statusBarInset)
                .fillMaxWidth()
                .height(navBarHeight),
            // The lambda has to capture topBar: a new instance on every change is what makes UIKitView run
            // update again. Reading only shownTopBar, a plain field, left the bar empty when the screen
            // was opened after the first frame.
            update = { bar ->
                // Read before the fade, which sets the model alpha to its target at once.
                val wasHidden = bar.alpha < 1.0
                navTinter.apply(bar, tint)
                (topBar ?: shownTopBar.value)?.let { navSynchronizer.sync(bar, it) }
                if (topBar != null && wasHidden) bar.settleLayout()
                navFader.fade(bar, visible = topBar != null)
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
                // Parked below the bottom edge, see the navigation bar above.
                .offset { IntOffset(0, if (tabBarOnScreen) 0 else barHeight.roundToPx()) }
                .fillMaxWidth()
                .height(barHeight),
            update = { bar ->
                // Read before the fade, which sets the model alpha to its target at once.
                val wasHidden = bar.alpha < 1.0
                bar.overrideUserInterfaceStyle = if (dark) {
                    UIUserInterfaceStyle.UIUserInterfaceStyleDark
                } else {
                    UIUserInterfaceStyle.UIUserInterfaceStyleLight
                }
                tinter.apply(bar, tint)
                // While the bar is hidden or about to come back, nothing about its items may animate:
                // a selection changed off screen would otherwise move the pill during the fade-in.
                synchronizer.sync(
                    bar,
                    items,
                    selectedIndex,
                    generation = foregroundCount,
                    withoutAnimation = wasHidden || !barVisible,
                )
                val height = bar.sizeThatFits(CGSizeMake(0.0, 0.0)).useContents { height }.dp
                if (height != measuredHeight) measuredHeight = height
                if (barVisible && wasHidden) bar.settleLayout()
                tabFader.fade(bar, visible = barVisible)
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = true,
                placedAsOverlay = true,
            ),
        )
    }
}

/**
 * Sets the accent on a bar. Without an accent the bar keeps what it inherits from the window, which
 * is the app's own tint if it has one. If an accent was set before and is withdrawn, the inherited
 * tint is put back by hand, because UIKit's `tintColor` of a Kotlin view cannot be set to nil.
 */
@OptIn(ExperimentalForeignApi::class)
private class BarTinter {
    private var applied = false

    fun apply(bar: UIView, tint: UIColor?) {
        if (tint != null) {
            bar.tintColor = tint
            applied = true
        } else if (applied) {
            bar.tintColor = bar.superview?.tintColor ?: UIColor.blueColor
            applied = false
        }
    }
}

/** The accent as a UIColor, or null if none is set. */
private fun Color.toUIColorOrNull(): UIColor? {
    if (!isSpecified) return null
    val srgb = convert(ColorSpaces.Srgb)
    return UIColor.colorWithRed(
        red = srgb.red.toDouble(),
        green = srgb.green.toDouble(),
        blue = srgb.blue.toDouble(),
        alpha = srgb.alpha.toDouble(),
    )
}

private const val BarFadeSeconds = 0.25

/** Time the first frames after a return to the foreground get before the bar is rebuilt once more. */
private const val ForegroundSettleMillis = 400L

/**
 * Changes whenever the app becomes active, and once more shortly after, when the content below the
 * bar is drawn again.
 */
@Composable
private fun rememberForegroundCount(): Int {
    var activations by remember { mutableIntStateOf(0) }
    var settled by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> activations++ }
        onDispose { center.removeObserver(observer) }
    }
    LaunchedEffect(activations) {
        if (activations > 0) {
            delay(ForegroundSettleMillis)
            settled = activations
        }
    }
    return activations + settled
}

/**
 * Fades a native bar in or out, switches its touches off while it is invisible and decides when it
 * may leave the screen.
 *
 * Only the alpha is animated. A hidden bar is [parked] off the screen once its fade-out has finished,
 * so its interop wrapper no longer takes the touches of the Compose content below it. The shell treats
 * a visible bar as on screen regardless of [parked], so it is back in place in the same Compose frame
 * in which the fade-in starts. The move itself is a plain frame change that CMP applies without
 * animation, and since only the position changes, the bar is not laid out again.
 *
 * Earlier versions gave the hidden bar a height of 0 instead. Growing back to full size while the
 * fade-in ran made UIKit lay the tab bar out from a zero frame on every return, which is the likely
 * cause of a Liquid Glass selection pill that slid in from the left and of tab titles that were
 * sometimes missing. That cause is a hypothesis from the code, it was not seen in a debugger.
 */
private class BarFader(parked: Boolean) {
    /**
     * True while the bar is faded out and moved off the screen. The composition reads it to place the
     * bar, and only [fade] and its completion write it. Because [fade] runs inside the `update` block,
     * a change also reruns that block once, which then does nothing.
     */
    var parked by mutableStateOf(parked)
        private set

    // UIView animations are additive since iOS 8: a fade-out interrupted by a fade-in still calls its
    // completion, usually with finished = true. Only the latest fade may park the bar.
    private var fadeId = 0

    fun fade(bar: UIView, visible: Boolean) {
        bar.userInteractionEnabled = visible
        if (visible && parked) parked = false
        val target = if (visible) 1.0 else 0.0
        if (bar.alpha == target) return
        val id = ++fadeId
        UIView.animateWithDuration(
            duration = BarFadeSeconds,
            animations = { bar.alpha = target },
            completion = { _ -> if (!visible && id == fadeId) parked = true },
        )
    }
}

/**
 * Lays the bar out at its current size before it fades in, outside any animation, so the fade starts
 * from a finished layout with titles and selection in place.
 */
private fun UIView.settleLayout() {
    UIView.performWithoutAnimation {
        setNeedsLayout()
        layoutIfNeeded()
    }
}

/** Keeps the native items and the selection in line with the Compose state. */
@OptIn(ExperimentalForeignApi::class)
private class TabBarSynchronizer {
    private var signature: List<Pair<String, String>>? = null
    private var generation = 0

    /**
     * A new [generation] rebuilds the items even if nothing about them changed. With
     * [withoutAnimation], and always on a rebuild, items and selection are set without animation.
     */
    fun sync(
        bar: UITabBar,
        items: List<ShellItem>,
        selectedIndex: Int,
        generation: Int,
        withoutAnimation: Boolean,
    ) {
        val rebuild = generation != this.generation
        this.generation = generation
        if (withoutAnimation || rebuild) {
            UIView.performWithoutAnimation { apply(bar, items, selectedIndex, rebuild) }
        } else {
            apply(bar, items, selectedIndex, rebuild)
        }
    }

    private fun apply(bar: UITabBar, items: List<ShellItem>, selectedIndex: Int, rebuild: Boolean) {
        val newSignature = items.map { it.title to it.icon.sfSymbol }
        if (rebuild) bar.setNeedsLayout()
        if (rebuild || newSignature != signature) {
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
        // Badges are not part of the signature: a changed badge must not rebuild the items. The value
        // is set on the existing native items instead.
        // Titles are checked the same way, so a shown bar always carries them on its items.
        items.forEachIndexed { index, item ->
            val native = bar.items?.getOrNull(index) as? UITabBarItem ?: return@forEachIndexed
            if (native.title != item.title) native.title = item.title
            if (native.badgeValue != item.badge) native.badgeValue = item.badge
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

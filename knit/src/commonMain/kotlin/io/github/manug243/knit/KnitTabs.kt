package io.github.manug243.knit

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icon of a tab or an action. iOS shows the SF Symbol named [sfSymbol]. Android, Desktop and Web
 * draw the content, an [ImageVector] or any composable such as an icon from an own design system.
 *
 * @param sfSymbol name of an SF Symbol, for example `"list.bullet"`.
 * @param content draws the icon in the given tint, which is the color the bar wants for it, for
 * example the selected color of a tab. Use [Content] to draw a [KnitIcon] yourself, in a custom bar.
 */
@Immutable
public class KnitIcon(
    public val sfSymbol: String,
    private val content: @Composable (tint: Color) -> Unit,
) {
    /** An icon that is an [ImageVector] on platforms without SF Symbols. */
    public constructor(sfSymbol: String, vector: ImageVector) : this(
        sfSymbol,
        { tint -> Icon(imageVector = vector, contentDescription = null, tint = tint) },
    )

    /** Draws the icon on the platforms that do not use SF Symbols. [tint] defaults to the current content color. */
    @Composable
    public fun Content(tint: Color = LocalContentColor.current) {
        content(tint)
    }
}

/** Receiver of the `tabs` block of [KnitScaffold]. */
public class KnitTabsScope<T : KnitTab> internal constructor(
    // Called when the block declares a tab after the scaffold has already read the result, see tab().
    private val onLateDeclaration: () -> Unit,
) {
    internal val items: MutableList<KnitTabItem<T>> = mutableListOf()
    private var closed = false

    /** The scaffold has taken the items. From now on the scope only reports that the block ran again. */
    internal fun close() {
        closed = true
    }

    /**
     * Declares [tab] with its [title] and [icon]. The declaration order is the order in the bar.
     *
     * @param badge text on the tab's icon, for example a count of unread items. `null` shows none.
     * It can change at any time, the block runs again when a state it reads changes. iOS shows it
     * as the `badgeValue` of the native item, the Material bar and rail as a `Badge`.
     */
    public fun tab(tab: T, title: String, icon: KnitIcon, badge: String? = null) {
        if (closed) {
            // A composable block restarts on its own when a state it reads changes, without the
            // scaffold, and then runs on the scope of the last pass. The scaffold is asked to run
            // again and collects the tabs from scratch, so removed and reordered tabs are followed too.
            onLateDeclaration()
            return
        }
        require(items.none { it.tab == tab }) { "Tab $tab is declared twice" }
        items += KnitTabItem(tab, title, icon, badge)
    }
}

internal class KnitTabItem<T : KnitTab>(
    val tab: T,
    val title: String,
    val icon: KnitIcon,
    val badge: String?,
)

/**
 * Runs the `tabs` block and returns what it declared. Every pass starts with an empty scope, so the
 * result is exactly what the block declares now: a tab that is gone is gone, the order is the
 * current one, and a tab declared twice is an error.
 */
@Composable
internal fun <T : KnitTab> composeTabItems(block: @Composable KnitTabsScope<T>.() -> Unit): List<KnitTabItem<T>> {
    val lateDeclarations = remember { mutableIntStateOf(0) }
    // Reading it makes the caller follow a block that restarted on its own.
    lateDeclarations.intValue
    val scope = KnitTabsScope<T>(onLateDeclaration = { lateDeclarations.intValue++ })
    scope.block()
    scope.close()
    return scope.items.toList()
}

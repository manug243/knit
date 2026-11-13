package io.github.manug243.knit

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    // A composable block restarts on its own when a state it reads changes, without the scaffold,
    // and then runs again on the same scope. Declaring a tab a second time then replaces it.
    private val redeclare: Boolean = false,
) {
    internal val items: MutableList<KnitTabItem<T>> = mutableListOf()

    // Tells the scaffold that a restarted block changed a tab, see tab().
    internal var revision: Int by mutableIntStateOf(0)
        private set

    /**
     * Declares [tab] with its [title] and [icon]. The declaration order is the order in the bar.
     *
     * @param badge text on the tab's icon, for example a count of unread items. `null` shows none.
     * It can change at any time, the block runs again when a state it reads changes. iOS shows it
     * as the `badgeValue` of the native item, the Material bar and rail as a `Badge`.
     */
    public fun tab(tab: T, title: String, icon: KnitIcon, badge: String? = null) {
        val item = KnitTabItem(tab, title, icon, badge)
        val index = items.indexOfFirst { it.tab == tab }
        when {
            index < 0 -> items += item
            !redeclare -> throw IllegalArgumentException("Tab $tab is declared twice")
            else -> {
                val old = items[index]
                items[index] = item
                // Icons are often created inside the block, so they are compared by their symbol:
                // comparing instances would invalidate the scaffold on every pass.
                if (old.title != title || old.icon.sfSymbol != icon.sfSymbol || old.badge != badge) revision++
            }
        }
    }
}

internal class KnitTabItem<T : KnitTab>(
    val tab: T,
    val title: String,
    val icon: KnitIcon,
    val badge: String?,
)

internal fun <T : KnitTab> buildTabItems(block: KnitTabsScope<T>.() -> Unit): List<KnitTabItem<T>> =
    KnitTabsScope<T>().apply(block).items.toList()

/** Like [buildTabItems], but the block is composable so it can read titles from resources. */
@Composable
internal fun <T : KnitTab> composeTabItems(block: @Composable KnitTabsScope<T>.() -> Unit): List<KnitTabItem<T>> {
    val scope = remember { KnitTabsScope<T>(redeclare = true) }
    // Reading the revision makes the scaffold follow a block that restarts on its own.
    scope.revision
    scope.block()
    return scope.items.toList()
}

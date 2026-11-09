package io.github.manug243.knit

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
public class KnitTabsScope<T : KnitTab> internal constructor() {
    internal val items: MutableList<KnitTabItem<T>> = mutableListOf()

    /** Declares [tab] with its [title] and [icon]. The declaration order is the order in the bar. */
    public fun tab(tab: T, title: String, icon: KnitIcon) {
        require(items.none { it.tab == tab }) { "Tab $tab is declared twice" }
        items += KnitTabItem(tab, title, icon)
    }
}

internal class KnitTabItem<T : KnitTab>(
    val tab: T,
    val title: String,
    val icon: KnitIcon,
)

internal fun <T : KnitTab> buildTabItems(block: KnitTabsScope<T>.() -> Unit): List<KnitTabItem<T>> =
    KnitTabsScope<T>().apply(block).items.toList()

/** Like [buildTabItems], but the block is composable so it can read titles from resources. */
@Composable
internal fun <T : KnitTab> composeTabItems(block: @Composable KnitTabsScope<T>.() -> Unit): List<KnitTabItem<T>> {
    val scope = KnitTabsScope<T>()
    scope.block()
    return scope.items.toList()
}

package io.github.manug243.knit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icon of a tab. iOS shows the SF Symbol, Android and Desktop show the vector.
 *
 * @param sfSymbol name of an SF Symbol, for example `"list.bullet"`.
 * @param vector icon for platforms without SF Symbols.
 */
@Immutable
public class KnitIcon(
    public val sfSymbol: String,
    public val vector: ImageVector,
)

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

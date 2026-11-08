package io.github.manug243.knit

/**
 * Describes the top bar of a screen, see the `topBar` parameter of [KnitScaffold].
 *
 * On iOS this becomes a native `UINavigationBar` (Liquid Glass with the iOS 26 SDK) with the system
 * back button, on Android and Desktop a Material 3 top app bar.
 *
 * @param title shown in the center of the bar.
 */
public class KnitTopBar(public val title: String) {
    override fun equals(other: Any?): Boolean = other is KnitTopBar && other.title == title

    override fun hashCode(): Int = title.hashCode()

    override fun toString(): String = "KnitTopBar(title=$title)"
}

/**
 * Top bar content as the platform shells see it. [backTitle] is null on a tab's start screen,
 * where there is nothing to go back to. Otherwise it names the screen below, which iOS shows next
 * to the back arrow.
 */
internal class ShellTopBar(
    val title: String,
    val backTitle: String?,
    val onBack: () -> Unit,
)

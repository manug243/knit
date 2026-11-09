package io.github.manug243.knit

/**
 * Describes the top bar of a screen, see the `topBar` parameter of [KnitScaffold].
 *
 * On iOS this becomes a native `UINavigationBar` (Liquid Glass with the iOS 26 SDK) with the system
 * back button, on Android and Desktop a Material 3 top app bar.
 *
 * @param title shown in the center of the bar.
 * @param actions buttons at the trailing end of the bar, the first one is the outermost.
 */
public class KnitTopBar(
    public val title: String,
    public val actions: List<KnitAction> = emptyList(),
) {
    override fun toString(): String = "KnitTopBar(title=$title, actions=${actions.map { it.label }})"
}

/**
 * A button in the top bar. iOS shows the SF Symbol of [icon], Android and Desktop its vector.
 *
 * @param label accessibility label of the button.
 */
public class KnitAction(
    public val label: String,
    public val icon: KnitIcon,
    public val onClick: () -> Unit,
)

/**
 * Top bar content as the platform shells see it. [backTitle] is null on a tab's start screen,
 * where there is nothing to go back to. Otherwise it names the screen below, which iOS shows next
 * to the back arrow.
 */
internal class ShellTopBar(
    val title: String,
    val actions: List<KnitAction>,
    val backTitle: String?,
    val onBack: () -> Unit,
)

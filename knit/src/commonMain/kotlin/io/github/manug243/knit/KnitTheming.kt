package io.github.manug243.knit

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors of the bars. On Android, Desktop and Web the Material tab bar, rail and top bar use all of
 * them except [accent]. iOS always draws the native bars, which take their look from the system, so
 * only [accent] applies there. Create it with [KnitDefaults.colors].
 */
@Immutable
public class KnitColors internal constructor(
    public val tabBarContainer: Color,
    public val tabBarContent: Color,
    public val tabBarSelectedContent: Color,
    public val tabBarIndicator: Color,
    public val topBarContainer: Color,
    public val topBarContent: Color,
    /** Tint of the native tab bar and navigation bar on iOS. [Color.Unspecified] keeps the system tint. */
    public val accent: Color,
)

/** Defaults of [KnitScaffold]. */
public object KnitDefaults {
    /**
     * Colors taken from the surrounding `MaterialTheme`. Pass any of them to override just that one,
     * for example when the app has its own design system and maps its colors here.
     *
     * @param accent tint of the native bars on iOS: the selected tab, the back button and the actions.
     * The default keeps the system tint. The other platforms ignore it, they have the detailed
     * colors above.
     */
    @Composable
    public fun colors(
        tabBarContainer: Color = NavigationBarDefaults.containerColor,
        tabBarContent: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        tabBarSelectedContent: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        tabBarIndicator: Color = MaterialTheme.colorScheme.secondaryContainer,
        topBarContainer: Color = MaterialTheme.colorScheme.surface,
        topBarContent: Color = MaterialTheme.colorScheme.onSurface,
        accent: Color = Color.Unspecified,
    ): KnitColors = KnitColors(
        tabBarContainer = tabBarContainer,
        tabBarContent = tabBarContent,
        tabBarSelectedContent = tabBarSelectedContent,
        tabBarIndicator = tabBarIndicator,
        topBarContainer = topBarContainer,
        topBarContent = topBarContent,
        accent = accent,
    )
}

/** One tab of a custom tab bar, see the `tabBar` parameter of [KnitScaffold]. */
public class KnitTabBarItem internal constructor(
    public val title: String,
    public val icon: KnitIcon,
    /** Text on the tab's icon, or null for none. */
    public val badge: String?,
    public val selected: Boolean,
    public val onClick: () -> Unit,
)

/**
 * What a custom tab bar draws.
 *
 * @param wide true from 600 dp width on. Draw a rail at the start of the screen then, a bar at the
 * bottom otherwise.
 */
public class KnitTabBarState internal constructor(
    public val items: List<KnitTabBarItem>,
    public val wide: Boolean,
)

/** What a custom top bar draws, see the `topBarContent` parameter of [KnitScaffold]. */
public class KnitTopBarState internal constructor(
    public val title: String,
    public val actions: List<KnitAction>,
    /** True below a tab's start screen. [onBack] pops the screen. */
    public val showBack: Boolean,
    public val onBack: () -> Unit,
)

/** Colors and custom bars of [KnitScaffold] for the shells that draw Compose bars. */
internal class ShellStyle(
    val colors: KnitColors,
    val tabBar: (@Composable (KnitTabBarState) -> Unit)?,
    val topBarContent: (@Composable (KnitTopBarState) -> Unit)?,
)

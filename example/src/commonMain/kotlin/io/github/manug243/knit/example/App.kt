package io.github.manug243.knit.example

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.entryProvider
import io.github.manug243.knit.KnitAction
import io.github.manug243.knit.KnitDefaults
import io.github.manug243.knit.KnitIcons
import io.github.manug243.knit.KnitScaffold
import io.github.manug243.knit.KnitTopBar
import io.github.manug243.knit.rememberKnitNavigator

/**
 * Pantry, the demo of knit: a small shopping list app with sample data. Every screen shows one part
 * of the library, see the README.
 */
@Composable
fun App(openDetailOnStart: Boolean = false) {
    val store = remember { PantryStore() }
    val accent = store.accent
    // Follows the system, so the status bar icons that enableEdgeToEdge picks match the surface behind them.
    // The accent is also the Material primary, so Android shows it too.
    // The swatches are mid-tone, so white text reads on them in both schemes.
    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = accent, onPrimary = Color.White)
    } else {
        lightColorScheme(primary = accent, onPrimary = Color.White)
    }
    MaterialTheme(colorScheme = colorScheme) {
        val navigator = rememberKnitNavigator(AppTab.entries, RouteSavedStateConfiguration)
        // Lets the CI take a screenshot of a screen with the top bar without tapping through the app.
        LaunchedEffect(openDetailOnStart) {
            if (openDetailOnStart && navigator.isAtTabRoot) navigator.navigate(Route.ListDetail(store.lists.first().id))
        }

        // The window background comes from the Android theme, not from Compose. Without a surface the
        // area behind the status bar stays light in dark mode.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            KnitScaffold(
                navigator = navigator,
                tabs = {
                    tab(AppTab.Lists, "Lists", KnitIcons.List)
                    tab(AppTab.Inbox, "Inbox", KnitIcons.Notifications, badge = store.unreadCount.takeIf { it > 0 }?.toString())
                    tab(AppTab.Search, "Search", KnitIcons.Search)
                    tab(AppTab.Settings, "Settings", KnitIcons.Settings)
                },
                entryProvider = entryProvider {
                    entry<Route.Lists> {
                        ListsScreen(store) { navigator.navigate(Route.ListDetail(it.id)) }
                    }
                    entry<Route.ListDetail> { route ->
                        ListDetailScreen(store, route.listId) { navigator.navigate(Route.ItemDetail(it.id)) }
                    }
                    entry<Route.ItemDetail> { route ->
                        ItemDetailScreen(store, route.itemId) { navigator.back() }
                    }
                    entry<Route.Inbox> { InboxScreen(store) }
                    entry<Route.Search> {
                        SearchScreen(store) { navigator.navigate(AppTab.Lists, Route.ListDetail(it.id)) }
                    }
                    entry<Route.Settings> { SettingsScreen(store) { navigator.navigate(Route.About) } }
                    entry<Route.About> { AboutScreen() }
                },
                // Tab start screens and About get a plain bar from here. List detail sets its own with
                // KnitTopBarOverride, item detail has none.
                topBar = { key ->
                    when (key) {
                        Route.Lists -> KnitTopBar("Pantry")
                        Route.Inbox -> KnitTopBar(
                            "Inbox",
                            listOf(KnitAction("Mark all read", KnitIcons.Check) { store.markAllRead() }),
                        )
                        Route.Search -> KnitTopBar("Search")
                        Route.Settings -> KnitTopBar("Settings")
                        Route.About -> KnitTopBar("About")
                        else -> null
                    }
                },
                colors = KnitDefaults.colors(accent = accent, tabBarIndicator = accent.copy(alpha = 0.25f)),
            )
        }
    }
}

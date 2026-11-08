package io.github.manug243.knit.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import io.github.manug243.knit.KnitScaffold
import io.github.manug243.knit.LocalKnitContentPadding
import io.github.manug243.knit.rememberKnitNavigator

private val StripeColors = listOf(
    Color(0xFFE53935),
    Color(0xFFFDD835),
    Color(0xFF43A047),
    Color(0xFF1E88E5),
    Color(0xFF8E24AA),
)
private const val StripeCount = 40

/**
 * Demo of the knit API. The screens are high-contrast stripes that run edge to edge, so it is
 * visible whether the native bar on iOS floats over them.
 */
@Composable
fun App() {
    MaterialTheme {
        val navigator = rememberKnitNavigator(AppTab.entries, RouteSavedStateConfiguration)

        KnitScaffold(
            navigator = navigator,
            tabs = {
                tab(AppTab.Home, "Home", HomeIcon)
                tab(AppTab.Lists, "Lists", ListsIcon)
                tab(AppTab.Settings, "Settings", SettingsIcon)
            },
            entryProvider = entryProvider {
                entry<Route.Home> { StripeScreen("Home") { navigator.navigate(Route.Detail("Home", it)) } }
                entry<Route.Lists> { StripeScreen("Lists") { navigator.navigate(Route.Detail("Lists", it)) } }
                entry<Route.Settings> { StripeScreen("Settings") { navigator.navigate(Route.Detail("Settings", it)) } }
                entry<Route.Detail> { route -> DetailScreen(route, onBack = { navigator.back() }) }
            },
        )
    }
}

@Composable
private fun StripeScreen(title: String, onOpen: (Int) -> Unit) {
    val system = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    // The bar floats above the content on iOS, so the list pads its end by the bar's height.
    val contentPadding = PaddingValues(
        start = system.calculateStartPadding(layoutDirection),
        top = system.calculateTopPadding(),
        end = system.calculateEndPadding(layoutDirection),
        bottom = LocalKnitContentPadding.current.calculateBottomPadding(),
    )
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        items((0 until StripeCount).toList()) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(StripeColors[index % StripeColors.size])
                    .clickable { onOpen(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "$title $index", color = Color.Black)
            }
        }
    }
}

@Composable
private fun DetailScreen(route: Route.Detail, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StripeColors[route.index % StripeColors.size])
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        contentAlignment = Alignment.Center,
    ) {
        Button(onClick = onBack) {
            Text("${route.tab} ${route.index}: back")
        }
    }
}

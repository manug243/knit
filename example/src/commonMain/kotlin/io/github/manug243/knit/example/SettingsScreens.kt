package io.github.manug243.knit.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.manug243.knit.KnitModalBottomSheet
import io.github.manug243.knit.knitSafePadding
import kotlinx.coroutines.launch

/**
 * Settings tab: accent color (`KnitDefaults.colors(accent = ...)` plus the Material primary), a
 * `KnitModalBottomSheet`, a snackbar that sits in `knitSafeArea()` and a push to [AboutScreen].
 */
@Composable
fun SettingsScreen(store: PantryStore, onOpenAbout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    SnackbarBox { snackbar ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = knitSafePadding()) {
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Accent color", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (choice in AccentChoices) {
                            val selected = store.accent == choice.color
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(choice.color)
                                    .border(
                                        BorderStroke(3.dp, if (selected) MaterialTheme.colorScheme.onSurface else choice.color),
                                        CircleShape,
                                    )
                                    .clickable { store.accent = choice.color },
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(top = 12.dp))
            }
            item { SettingsRow("Show sheet", "A modal sheet, the tab bar leaves") { showSheet = true } }
            item {
                SettingsRow("Show snackbar", "Sits above the tab bar") {
                    scope.launch { snackbar.showSnackbar("Hello from knit") }
                }
            }
            item { SettingsRow("About", "One level down, plain top bar", onClick = onOpenAbout) }
        }
        if (showSheet) {
            KnitModalBottomSheet(onDismissRequest = { showSheet = false }) {
                Column(
                    Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Modal sheet", style = MaterialTheme.typography.titleLarge)
                    Text("On iOS the native tab bar floats above everything Compose draws. KnitModalBottomSheet hides it while this sheet is open.")
                    Text("Drag down, tap outside or press the button to close.")
                    Button(onClick = { showSheet = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column {
        Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()
    }
}

/** Second level of the Settings tab, with a plain `topBar`-parameter title (see [App]). */
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(knitSafePadding()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Pantry", style = MaterialTheme.typography.headlineMedium)
        Text("A demo of knit: native tab bar and navigation bar on iOS, Material bars elsewhere, one Compose tree and Navigation 3 underneath.")
        Text("All data is sample data kept in memory.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

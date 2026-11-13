package io.github.manug243.knit.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.manug243.knit.KnitIcons
import io.github.manug243.knit.knitSafeArea

/**
 * Search tab. The field filters the items of all lists. `knitSafeArea()` keeps the screen clear of
 * the bars and counts those insets as consumed, so the `imePadding()` inside only adds the keyboard:
 * the results shrink above it while the tab bar leaves.
 */
@Composable
fun SearchScreen(store: PantryStore, onOpenList: (PantryList) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = store.items.filter { it.name.contains(query.trim(), ignoreCase = true) }
    Column(Modifier.fillMaxSize().knitSafeArea().imePadding()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search all lists") },
            leadingIcon = { KnitIcons.Search.Content() },
            singleLine = true,
        )
        if (results.isEmpty()) {
            Text(
                "Nothing found for \"${query.trim()}\"",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(results, key = { it.id }) { item ->
                val list = store.list(item.listId) ?: return@items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenList(list) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(list.color))
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge)
                        Text(list.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(Modifier.padding(start = 48.dp))
            }
        }
    }
}

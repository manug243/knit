package io.github.manug243.knit.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.manug243.knit.KnitAction
import io.github.manug243.knit.KnitIcons
import io.github.manug243.knit.KnitTabReselectEffect
import io.github.manug243.knit.KnitTopBar
import io.github.manug243.knit.KnitTopBarOverride
import io.github.manug243.knit.knitSafeArea
import kotlinx.coroutines.launch

/**
 * Lists tab, start screen. A long list of colored cards that scroll behind the bars (`knitSafePadding`
 * as content padding). Tapping the tab again scrolls back to the top (`KnitTabReselectEffect`).
 */
@Composable
fun ListsScreen(store: PantryStore, onOpenList: (PantryList) -> Unit) {
    val listState = rememberLazyListState()
    KnitTabReselectEffect { listState.animateScrollToItem(0) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = listPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(store.lists, key = { it.id }) { list ->
            val listItems = store.itemsOf(list.id)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(list.color)
                    .clickable { onOpenList(list) }
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Text(list.name, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${listItems.size} items", color = Color.White.copy(alpha = 0.9f), modifier = Modifier.weight(1f))
                    Text("${listItems.count { it.checked }} done", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}

/**
 * Second level of the Lists tab. The screen sets its own top bar with `KnitTopBarOverride`: the
 * title is the list's name, plus two actions.
 */
@Composable
fun ListDetailScreen(store: PantryStore, listId: Int, onOpenItem: (PantryItem) -> Unit) {
    val list = store.list(listId) ?: return
    val scope = rememberCoroutineScope()
    SnackbarBox { snackbar ->
        KnitTopBarOverride(
            KnitTopBar(
                title = list.name,
                actions = listOf(
                    KnitAction("Add item", KnitIcons.Add) { store.addItem(listId) },
                    KnitAction("Share", KnitIcons.Share) { scope.launch { snackbar.showSnackbar("Shared") } },
                ),
            ),
        )
        val listItems = store.itemsOf(listId)
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = listPadding()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(list.color)
                        .padding(20.dp),
                ) {
                    Text(list.name, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("${listItems.count { it.checked }} of ${listItems.size} done", color = Color.White.copy(alpha = 0.9f))
                }
            }
            items(listItems, key = { it.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenItem(item) }.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = item.checked, onCheckedChange = { item.checked = it })
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                            color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

/**
 * Third level of the Lists tab, and a test case: it has no knit top bar on purpose. It draws its own
 * header in Compose with a back button at the top left, inside `knitSafeArea()`. On iOS a Compose
 * button in that spot used to be dead because the hidden native bar sat on top of it.
 */
@Composable
fun ItemDetailScreen(store: PantryStore, itemId: Int, onBack: () -> Unit) {
    val item = store.item(itemId)
    val list = item?.let { store.list(it.listId) }
    Column(Modifier.fillMaxSize().knitSafeArea()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ ${list?.name ?: "Back"}") }
        }
        if (item == null || list == null) {
            Text("This item is gone.", modifier = Modifier.padding(24.dp))
            return@Column
        }
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(list.color))
            Text(item.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("On ${list.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.note)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bought", Modifier.weight(1f))
                Switch(checked = item.checked, onCheckedChange = { item.checked = it })
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Done") }
            OutlinedButton(
                onClick = {
                    store.removeItem(item.id)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Remove from list") }
        }
    }
}

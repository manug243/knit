package io.github.manug243.knit.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.manug243.knit.knitSafePadding

/**
 * Inbox tab. Tapping a notice marks it read. The unread count is the badge of the tab and the top
 * bar has a "mark all read" action, both set in [App].
 */
@Composable
fun InboxScreen(store: PantryStore) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = knitSafePadding()) {
        items(store.notices, key = { it.id }) { notice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { notice.read = true }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (notice.read) Color.Transparent else MaterialTheme.colorScheme.primary),
                )
                Text(
                    notice.text,
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notice.read) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (notice.read) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
            HorizontalDivider(Modifier.padding(start = 46.dp))
        }
    }
}

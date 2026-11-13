package io.github.manug243.knit.example

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class PantryList(val id: Int, val name: String, val color: Color)

@Stable
class PantryItem(val id: Int, val listId: Int, val name: String, val note: String, checked: Boolean = false) {
    var checked by mutableStateOf(checked)
}

@Stable
class Notice(val id: Int, val text: String, read: Boolean = false) {
    var read by mutableStateOf(read)
}

class AccentChoice(val name: String, val color: Color)

val AccentChoices = listOf(
    AccentChoice("Blue", Color(0xFF1E6FE0)),
    AccentChoice("Green", Color(0xFF2E9E5B)),
    AccentChoice("Orange", Color(0xFFE8710A)),
    AccentChoice("Pink", Color(0xFFD6336C)),
    AccentChoice("Purple", Color(0xFF7B4DD6)),
)

/**
 * In-memory data of the demo, a plain state holder that [App] remembers. Nothing is saved, a
 * restart brings the sample data back.
 */
@Stable
class PantryStore {
    val lists: List<PantryList> = SampleLists.mapIndexed { index, list -> PantryList(index, list.name, list.color) }
    val items = mutableStateListOf<PantryItem>()
    val notices = mutableStateListOf<Notice>()

    /** The accent color picked in the settings. */
    var accent by mutableStateOf(AccentChoices.first().color)

    private var nextItemId = 0
    private var added = 0

    init {
        SampleLists.forEachIndexed { listId, list ->
            list.items.forEachIndexed { index, entry ->
                val (name, note) = entry.split("|")
                items += PantryItem(nextItemId++, listId, name, note, checked = index % 4 == 3)
            }
        }
        SampleNotices.forEachIndexed { index, text -> notices += Notice(index, text, read = index >= 4 && index % 3 == 0) }
    }

    val unreadCount: Int get() = notices.count { !it.read }

    fun list(id: Int): PantryList? = lists.firstOrNull { it.id == id }

    fun item(id: Int): PantryItem? = items.firstOrNull { it.id == id }

    fun itemsOf(listId: Int): List<PantryItem> = items.filter { it.listId == listId }

    /** Adds a made-up item, the demo has no text input for it. */
    fun addItem(listId: Int) {
        val (name, note) = ExtraItems[added++ % ExtraItems.size].split("|")
        items += PantryItem(nextItemId++, listId, name, note)
    }

    fun removeItem(id: Int) {
        items.removeAll { it.id == id }
    }

    fun markAllRead() {
        notices.forEach { it.read = true }
    }
}

private val ExtraItems = listOf(
    "Basil|1 pot", "Honey|Jar", "Tofu|2 blocks", "Lemons|4", "Oat milk|2 l", "Dark chocolate|3 bars",
    "Olive oil|Extra virgin", "Spinach|Fresh, 300 g",
)

private val SampleNotices = listOf(
    "Milk is running low",
    "Eggs expire on Friday",
    "Anna added 3 items to Weekend BBQ",
    "Pasta is back in stock at your store",
    "Reminder: Sunday brunch list is not done yet",
    "Olive oil is on sale this week",
    "Your Pharmacy list has 2 open items",
    "Ben checked off Bread",
    "Rice has been in the pantry for 6 months",
    "Weekly review: you checked off 23 items",
)

private class SampleList(val name: String, val color: Color, val items: List<String>)

// Items are "name|note".
private val SampleLists = listOf(
    SampleList("Weekly groceries", Color(0xFFD32F2F), listOf("Milk|2 l, whole", "Eggs|12, free range", "Bread|Sourdough", "Butter|250 g", "Tomatoes|1 kg", "Yogurt|4 cups")),
    SampleList("Weekend BBQ", Color(0xFFE64A19), listOf("Sausages|8 pieces", "Charcoal|5 kg", "Corn|4 cobs", "Burger buns|6", "Ketchup|Large", "Lemonade|3 bottles", "Halloumi|2 packs")),
    SampleList("Bakery", Color(0xFFF57C00), listOf("Flour|1 kg, type 550", "Yeast|2 packets", "Sugar|500 g", "Vanilla|1 pod", "Baking powder|1 pack")),
    SampleList("Fruit and veg", Color(0xFF388E3C), listOf("Apples|6, Elstar", "Bananas|1 bunch", "Carrots|500 g", "Cucumber|2", "Avocados|3, ripe", "Berries|Mixed, 250 g", "Zucchini|2")),
    SampleList("Pantry basics", Color(0xFF00796B), listOf("Rice|2 kg", "Pasta|Penne, 1 kg", "Lentils|500 g", "Canned tomatoes|6", "Salt|Fine", "Olive oil|1 l")),
    SampleList("Breakfast", Color(0xFF0097A7), listOf("Oats|1 kg", "Honey|Jar", "Coffee|Beans, 500 g", "Orange juice|1 l", "Muesli|Fruit")),
    SampleList("Pharmacy", Color(0xFF1976D2), listOf("Ibuprofen|20 tablets", "Plasters|Assorted", "Toothpaste|2", "Vitamin D|Drops")),
    SampleList("Sunday brunch", Color(0xFF303F9F), listOf("Croissants|6", "Smoked salmon|200 g", "Cream cheese|Natural", "Prosecco|1 bottle", "Chives|1 bunch", "Bagels|4")),
    SampleList("Pet supplies", Color(0xFF5E35B1), listOf("Cat food|12 cans", "Litter|10 l", "Dog treats|Chicken", "Chew toy|Rope")),
    SampleList("Cleaning", Color(0xFF8E24AA), listOf("Dish soap|Lemon", "Sponges|5", "Laundry pods|40", "Glass cleaner|Spray", "Trash bags|35 l, 20")),
    SampleList("Dinner party", Color(0xFFC2185B), listOf("Lamb|1.5 kg", "Potatoes|2 kg", "Red wine|3 bottles", "Rosemary|Fresh", "Cream|500 ml", "Tiramisu|Ingredients", "Candles|Unscented")),
    SampleList("Snacks", Color(0xFFAD1457), listOf("Popcorn|Salted", "Nuts|Mixed, 400 g", "Crackers|2 packs", "Hummus|2", "Chips|Paprika")),
    SampleList("Frozen", Color(0xFF455A64), listOf("Peas|1 kg", "Pizza|3", "Ice cream|Vanilla", "Fish fingers|1 pack")),
    SampleList("Drinks", Color(0xFF6D4C41), listOf("Sparkling water|12 bottles", "Tea|Green, Earl Grey", "Beer|2 crates", "Ginger beer|6")),
)

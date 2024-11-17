// Total complexity: 6
class Example {
  enum class Items {
    ITEM_1,
    ITEM_2,
    ITEM_3,
    ITEM_4,
    ITEM_5
  }

  // Complexity[chooseItem]: 1 + 5 (case) = 6
  fun chooseItem(item: Items) {
    when (item) {
      Items.ITEM_1 -> println("Item 1")
      Items.ITEM_2 -> println("Item 2")
      Items.ITEM_3 -> println("Item 3")
      Items.ITEM_4 -> println("Item 4")
      Items.ITEM_5 -> println("Item 5")
    }
  }
}
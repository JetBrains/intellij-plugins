// Total complexity: 5 + 1 + 3 = 9
public class Example {
  enum Items {
    ITEM_1,
    ITEM_2,
    ITEM_3,
    ITEM_4,
    ITEM_5
  }

  // Complexity[noDefault]: 1 + 4 (case) = 5
  private void chooseItem(Items item) {
    switch (item) {
      case ITEM_1:
        System.out.println("Item 1");
        break;
      case ITEM_2:
        System.out.println("Item 2");
        break;
      case ITEM_3:
        System.out.println("Item 3");
        break;
      case ITEM_4:
        System.out.println("Item 4");
        break;
      case ITEM_5:
        System.out.println("Item 5");
        break;
    }
  }

  // Complexity[onlyDefault]: 1
  private void onlyDefault(Items item) {
    switch (item) {
      default:
        break;
    }
  }

  // Complexity[chooseItem]: 1 + 2 (case) = 3
  private void chooseItem(Items item) {
    switch (item) {
      case ITEM_1:
        System.out.println("Item 1");
        break;
      case ITEM_2:
        System.out.println("Item 2");
        break;
      default:
        break;
    }
  }
}

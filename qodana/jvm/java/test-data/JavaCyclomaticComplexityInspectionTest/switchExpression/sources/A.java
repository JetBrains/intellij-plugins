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
  private void noDefault(Items item) {
    final String printMessage = switch (item) {
      case ITEM_1 -> "Item 1";
      case ITEM_2 -> "Item 2";
      case ITEM_3 -> "Item 3";
      case ITEM_4 -> "Item 4";
      case ITEM_5 -> "Item 5";
    };
    System.out.println(printMessage);
  }

  // Complexity[noDefault]: 1
  private void onlyDefault(Items item) {
    final String printMessage = switch (item) {
      default -> "Default Item";
    };
    System.out.println(printMessage);
  }

  // Complexity[caseAndDefault]: 1 + 2 (case) = 3
  private void caseAndDefault(Items item) {
    final String printMessage = switch (item) {
      case ITEM_1 -> "Item 1";
      case ITEM_2 -> "Item 2";
      default -> "Default Item";
    };
    System.out.println(printMessage);
  }
}

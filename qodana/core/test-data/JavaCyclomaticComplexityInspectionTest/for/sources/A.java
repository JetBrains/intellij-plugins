import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Total complexity: 2 + 4 = 4
public class Example {
  // Complexity[generateList]: 1 + 1 (for) = 2
  private static  List<Integer> generateList(int n) {
    final List<Integer> listToFilter = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      final Random random = new Random();
      final int number = random.nextInt(0, 100);
      listToFilter.add(number);
    }
    return listToFilter;
  }

  // Complexity[main]: 1 + 1 (for) = 2
  public static void main(String[] args) {
    final List<Integer> listToFilter = generateList(10);
    for (int num: listToFilter) {
      System.out.println(num);
    }
  }
}

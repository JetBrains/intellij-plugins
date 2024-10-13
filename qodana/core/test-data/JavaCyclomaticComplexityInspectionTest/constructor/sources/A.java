import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

// Total complexity: 4
public class Example {
  private final ArrayList<Integer> myList;
  // Complexity[Example]: 1 + 1 (for) + 2 (if) = 4
  Example(List<Integer> list) {
    myList = new ArrayList<>();
    for (Integer i : list) {
      System.out.println(i);
      if (i % 2 == 0) {
        myList.add(i);
      } else if (i % 3 == 0) {
        continue;
      }
    }
  }
}
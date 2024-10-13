import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Total complexity: 1 + 4 = 5
public class Example {
  // Complexity[main]: 1
  public static void main(String[] args) {
    final List<String> listToSort = new ArrayList<>(List.of("someWord", "abc", "randomWord", "ccc"));
    listToSort.sort(new Comparator<String>() {
      // Complexity[compare]: 1 + 3 (if) = 4
      @Override
      public int compare(String o1, String o2) {
        if (o1.length() > o2.length()) {
          return 1;
        }
        else if (o1.length() < o2.length()) {
          return -1;
        }
        else if (o1.equals(o2)) {
          return 0;
        }
        return 0;
      }
    });
  }
}

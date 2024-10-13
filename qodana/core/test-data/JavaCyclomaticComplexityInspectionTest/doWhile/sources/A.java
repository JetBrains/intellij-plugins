// Total complexity: 4
public class Example {
  // Complexity[main]: 1 + 2 (do while) + 1 (if) = 4
  public static void main(String[] args) {
    int a = 5;
    do {
      do {
        // do something ...
        if (a % 2 == 0) {
          System.out.printf("a");
        }
      } while (true);
    } while (true);
  }
}

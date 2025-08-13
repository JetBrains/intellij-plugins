// Total complexity: 9
public class Example {
  // Complexity[main]: 1 + 4 (if) + 2 (||) + 2 (&&) = 9
  public static void main(String[] args) {
    int a = 5;
    if (a % 2 == 0 || a % 3 == 0) {
      if (a == 4) {
        // do something ...
      }
    } else if (a % 3 == 0 && a != 6 || a % 3 == 0 && a != 9) {
      // do something ...
    } else if (a % 4 == 0) {
      // do something ...
    } else {
      // do something ...
    }
  }
}
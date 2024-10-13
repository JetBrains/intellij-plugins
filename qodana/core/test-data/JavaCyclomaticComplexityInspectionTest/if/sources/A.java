// Total complexity: 7
public class Example {
  // Complexity[main]: 1 + 6 (if) = 7
  public static void main(String[] args) {
    int a = 5;
    if (a % 2 == 0) {
      if (a == 4) {
        // do something ...
      }
    } else if (a % 3 == 0) {
      if (a == 3) {
        // do something ...
      } else if (a == 6) {
        // do something ...
      }
    } else if (a % 4 == 0) {
      // do something ...
    } else {
      // do something ...
    }
  }
}

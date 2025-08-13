// Total complexity: 3
public class Example {

  // Complexity[chooseItem]: 1 + 2 (ternary) = 3
  private int selectA(int a) {
    return (a > 5) ? (a < 10) ? a + 7 : a + 5 : a - 10;
    /*
     * if (a > 5) {
     *      if (a < 10) {
     *          a + 7
     *      } else {
     *          a + 5
     *      }
     * } else {
     *      a - 10;
     * }
     */
  }
}

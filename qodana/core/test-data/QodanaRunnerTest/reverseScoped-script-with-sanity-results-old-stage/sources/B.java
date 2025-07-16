class B {
  public static void main() {
    if (true) {
    }
    unusedResult();
    unusedResult();
  }
  public static int unusedResult() {
    return 5;
  }
}
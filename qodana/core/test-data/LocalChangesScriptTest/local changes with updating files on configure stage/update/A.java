class A {
  public static void main() {
    unusedResult(1);
    unusedResult(2);
    unusedResult2(3);
  }

  public static int unusedResult(int i) {
    return 5;
  }
  public static int unusedResult2(int i) {
    return 5;
  }
}
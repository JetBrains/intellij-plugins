class A {
  public static void main() {
    unusedResult(1);
    unusedResult(2);
    unusedResult(3);
  }

  public static int unusedResult(int i) {
    return 5;
  }
}
class A {
  public static void main() {
    unusedResult();
    unusedResult();
    unusedResult();
  }

  public static int unusedResult() {
    return 5;
  }

  public static int unusedResult2() {
    return 5;
  }
}
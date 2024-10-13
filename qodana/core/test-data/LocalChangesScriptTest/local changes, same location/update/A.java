class A {
  public static void main() {
    unusedResult();
    unusedResult();
    unusedResult();
  }

  public static int unusedResult() {
    return 5;
  }
}
class A {
  public static void main() {
    unusedResult();

    unusedResult();
  }

  public static int unusedResult() {
    return 5;
  }
}
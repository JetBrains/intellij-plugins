class A {
  public static void main() {
    System.out.println("Hello world");
    if (1 == 1) {
      System.out.println("Another");
    }
    new Thread()
    unusedResult();
    unusedResult();
  }

  public static int unusedResult() {
    return 5;
  }
}
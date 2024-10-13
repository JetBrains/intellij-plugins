class A {
  public static void main() {
    System.out.println("Hello world");

    System.out.println("Another");

    unusedResult();
    unusedResult();
  }

  public static int unusedResult() {
    return 5;
  }
}
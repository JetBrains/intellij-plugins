class A {
  public static void main() {
    System.out.println("Hello world");
    if (1 == 1) {
      System.out.println("Another");
    }
    unusedMethod();
    unusedMethod();
  }

  public static int unusedMethod() { return 5; }
}

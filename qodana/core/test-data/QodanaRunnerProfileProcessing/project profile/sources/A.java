class A {
  private int a = 0;

  public static void main() {
    System.out.println("Hello world");

    System.out.println("Another " + a);

    unusedResult();
    unusedResult();
  }

  public int unusedZeroResult() {
    return 0;
  }

  public static int unusedResult() {
    unusedZeroResult();
    return 5;
  }
}
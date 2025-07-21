class A {
  private int a = 0;
  private long l = 10L;

  public static void main() {
    System.out.println("Hello world");
    System.out.println("a can be final" + a);
    System.out.println("l can be final" + l);

    if (0 == 0) {
      System.out.println("ConstantValue");
    }

    if (2 == 2) {
      System.out.println("ConstantValue");
    }
  }

  public int unusedAssignment() {
    int i = 1;
    i = 2;
    return 0;
  }
}
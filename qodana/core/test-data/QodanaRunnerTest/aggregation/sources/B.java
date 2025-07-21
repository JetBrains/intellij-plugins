class B {
  private int b = 0;

  public static void main() {
    System.out.println("b can be final" + b);
  }

  public static void constantValue() {
    if (0 < 1) {
      System.out.println("ConstantValue");
    }
  }
}
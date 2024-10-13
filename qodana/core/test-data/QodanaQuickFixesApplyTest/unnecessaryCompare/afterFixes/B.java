class B {
  private static int field = 0;

  public static void main(String[] args) {
    if (isCustomElement(field)) {
      field = 1;
    }
  }

  public static boolean isCustomElement(int i) {
    return i == 5;
  }
}
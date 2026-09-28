class A {

  private static String msg = "MyMessage"

  class InnerA {
    static void printMsg() {
      System.out.println(msg);
    }
  }
}
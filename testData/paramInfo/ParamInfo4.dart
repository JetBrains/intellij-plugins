class ParamInfo4 {
  main() {
    foo(123123<caret>,123123);
  }

  static foo(int x, int y) {
      return "test";
  }
}
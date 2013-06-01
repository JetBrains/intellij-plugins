class ParamInfo3 {
  main() {
    foo(12<caret>3123,123123);
  }

  static foo(int x, int y) {
      return "test";
  }
}
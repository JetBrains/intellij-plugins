class ParamInfo6 {
  main() {
    foo(123123,<caret>);
  }

  static foo(int x, {int y = 239}) {
      return "test";
  }
}
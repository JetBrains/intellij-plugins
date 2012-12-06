class ParamInfo6 {
  main() {
    add(<caret>abs(-123), 123);
  }

  static abs(int x) {
      return x >= 0 ? x : -x;
  }

  static add(int x, int y = 239) {
      return x + y;
  }
}
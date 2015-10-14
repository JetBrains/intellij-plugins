class A {
  int f;

  main() {
    <caret>foo = 1;
    foo = 2 + 3;
  }

  void set foo(int x) {
    f = x * 5;
  }
}
class A {
  static set foo(int value) {
    <caret>
  }
}

main() {
  A.foo = 0;
}
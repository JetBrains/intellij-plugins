class A {
  static set foo(value) {
    <caret>
  }
}

main() {
  A.foo = 0;
}
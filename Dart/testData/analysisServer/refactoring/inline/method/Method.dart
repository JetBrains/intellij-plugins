class A {
  main() {
    var a = foo(1, 2);
    var b = <caret>foo(10, 20) * 5;
  }

  foo(int a, int b) {
    return a + b;
  }
}
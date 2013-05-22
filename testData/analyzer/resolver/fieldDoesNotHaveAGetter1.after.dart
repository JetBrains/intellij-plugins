class A {
  static get foo => <caret>;

  static set foo(x) {}
}

main() {
  print(A.foo);
}
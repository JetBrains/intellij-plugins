class A {
  static get f => <caret>;

  static set f(x) {}
}

main() {
  print(A.f);
}
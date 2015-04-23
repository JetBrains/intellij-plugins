class A {
  A.named({test});
}

main() {
  new A.named(te<caret>st: 42);
}
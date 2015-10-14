class A {
  test<caret>() {}
}

main(A a, b) {
  a.test();
  b.test();
}
class Foo {
  Foo.bar(){}

  test() {}
}

main() {
  var tmp = new Foo.bar();
  tmp.te<caret>st();
}
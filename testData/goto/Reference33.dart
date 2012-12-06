class Foo {
  test() {}
}

main() {
  var createInstance = () => new Foo();
  var tmp = createInstance();
  tmp.te<caret>st();
}
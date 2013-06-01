class Foo {
  static createInstance() => new Foo();

  test() {}
}

main() {
  var tmp = Foo.createInstance();
  tmp.te<caret>st();
}
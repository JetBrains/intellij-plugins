class Foo {
  Bar getBar() {}
}

class Bar {
  test(){}
}

main() {
  var foo = new Foo();
  foo.getBar()..te<caret>st();
}
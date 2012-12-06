class Foo {
  Bar get bar() {}
}

class Bar {
  test(){}
}

main() {
  var foo = new Foo();
  foo.bar..te<caret>st();
}
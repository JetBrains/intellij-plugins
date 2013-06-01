class Foo {
  Bar get bar() {}
}

class Bar {
  Bar test() {}
}

main() {
  var foo = new Foo();
  foo.bar..test()..te<caret>st();
}
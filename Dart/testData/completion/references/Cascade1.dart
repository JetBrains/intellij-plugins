class Foo {
  Bar get bar() {}
}

class Bar {
  Bar test1() {}
  Bar test2() {}
  get test3 {}
}

main() {
  var foo = new Foo();
  foo.bar..test1().test2()..test2().test3..te<caret>;
}
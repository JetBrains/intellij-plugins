class Foo {
  Bar get bar() {}
  Bar bar2() {}
}

class Bar {
  Bar test1() {}
  Bar test2() {}
  get test3 {}
}

main() {
  var foo = new Foo();
  foo..test1().test2()..test2().test3..b<caret>;
}
class Foo {
  Foo.fromParams(String one, [String two, String three]) {}

  void foo2() {
      Foo foo = new Foo.fromParams("foo", <caret>);
  }
}
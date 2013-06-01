class Foo {
  void doIt(String one, [String two, String three]) {}

  void foo2() {
      Foo foo = new Foo();
      foo.doIt(<caret>);
  }
}
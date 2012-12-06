class Foo {
  int bar;

  foo() {
    <selection>var i = 0;
    i = bar - 1;</selection>
    return i + 10 + bar;
  }
}
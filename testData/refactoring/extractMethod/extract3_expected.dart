class Foo {
  int bar;

  int extracted() {
    var i = 0;
    i = bar - 1;
    return i;
  }

  foo() {
    var i = extracted();
    return i + 10 + bar;
  }
}
class Foo {
  int bar;

  extracted() {
    var unused = 0;
    print("one");
    print("two");
  }

  foo() {
    extracted();
    return i + 10 + bar;
  }
}
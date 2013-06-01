class Foo {
  get bar => <caret>;

  set bar(Bar value) {}
}

class Bar {}

main() {
  var foo = new Foo();
  var bar = foo.bar;
}
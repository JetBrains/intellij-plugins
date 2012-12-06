class Foo {
  set bar(Bar value) {}
}

class Bar {}

main() {
  var foo = new Foo();
  var bar = foo.bar;
}
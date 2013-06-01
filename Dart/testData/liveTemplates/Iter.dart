main() {
  var arr = new Foo();
  iter<caret>
}

class Foo {
  iterator() => null;
}
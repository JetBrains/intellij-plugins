class Foo<T> {
  T item() => null;
  Foo<T> copy() => null;
}

class Bar {
  bar(){}
}

main() {
  var foo = new Foo<Bar>();
  var tmp = foo.copy().item();
  tmp.ba<caret>r;
}
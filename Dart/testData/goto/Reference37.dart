void main() {
  var foo = new Foo()..bar = 239
                     ..baz.getBar().getB<caret>az();
}

class Foo {
  Bar bar;
  Baz baz;
}

class Bar {
  Baz getBaz() => null;
}
class Baz {
  Bar getBar() => null;
}
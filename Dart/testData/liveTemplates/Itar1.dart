main() {
  var arr = new Foo();
  for<caret>
}

class Foo {
  get length() => 0;
  operator []() => this;
}

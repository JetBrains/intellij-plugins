main() {
  var arr = new Foo();
  itar<caret>
}

class Foo {
  get length() => 0;
  operator []() => this;
}
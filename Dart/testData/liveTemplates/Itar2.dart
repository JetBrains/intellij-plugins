main() {
  var arr = new Foo();
  for (i in [0, 1, 2, 3]) {
    itar<caret>
  }
}

class Foo {
  get length() => 0;
  operator []() => this;
}

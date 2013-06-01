main() {
  var arr = new Foo();
  for (var i = 0; i < arr.length; ++i) {
    var o = arr[i];

  }
}

class Foo {
  get length() => 0;

  operator []() => this;
}
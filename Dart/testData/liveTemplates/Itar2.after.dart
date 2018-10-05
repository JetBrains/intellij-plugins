main() {
  var arr = new Foo();
  for (i in [0, 1, 2, 3]) {
    for (var j = 0; j < arr.length; ++j) {
      var o = arr[j];
    }
  }
}

class Foo {
  get length() => 0;

  operator []() => this;
}

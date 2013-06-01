class Foo {
  operator negate() {
    for (int ij = 0; ij < 100; ij += 2) {
      print(i<caret>j);
    }
  }
}
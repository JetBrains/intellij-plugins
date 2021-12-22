void main() {
  new A(x: 1 != 2 ? true<caret>);
}

class A {
  A({bool? x});
}

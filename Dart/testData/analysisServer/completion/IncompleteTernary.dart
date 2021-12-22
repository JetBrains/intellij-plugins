void main() {
  new A(x: 1 != 2 ? tru<caret>);
}

class A {
  A({bool? x});
}

void main() {
  new A(x: true ? true<caret>);
}

class A {
  A({int x});
}

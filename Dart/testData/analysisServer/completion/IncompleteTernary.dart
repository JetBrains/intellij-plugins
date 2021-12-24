void main() {
  new A(x: true ? tr<caret>);
}

class A {
  A({int x});
}

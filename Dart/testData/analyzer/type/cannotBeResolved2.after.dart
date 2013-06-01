class Object {}
class A {
  static set noField(value) {
    <caret>
  }

  static var field = 239;
}

method() {
  A.noField = 1;
}
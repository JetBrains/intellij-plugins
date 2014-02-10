class Object {}
class A {
  static set noField(int value) {
    <caret>
  }

  static var field = 239;
}

method() {
  A.noField = 1;
}
class B {
  bool operator ==(other) => false;
}

class A extends B {
  bool operator <caret>==(other) => false;
}

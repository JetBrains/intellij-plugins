class B {
  bool operator <caret>==(other) => false;
}

class A extends B {
  bool operator ==(other) => false;
}

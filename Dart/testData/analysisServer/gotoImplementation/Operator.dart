class A {
  bool operator <caret>==(other) => false;
}

class B extends A {
  bool operator ==(other) => false;
}

class C extends A {
  bool operator ==(other) => false;
}

class D extends C {
  bool operator ==(other) => false;
}
class Foo {
  bool operator <caret>==(other) => false;
}

class Bar extends Foo {
  bool operator ==(other) => false;
}
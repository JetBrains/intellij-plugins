class Foo implements Base {
  f<caret>oo(){
  }
}

class Bar implements Base {
  foo(){
  }
}

class Base {
  foo() => null;
}
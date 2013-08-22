class Method implements Base {
  f<caret>oo(){
  }

  bar(){
    foo();
  }
}

class Base {
  foo() => null;
}
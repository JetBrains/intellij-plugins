class Method extends Base {
  foo(){
  }

  bar(){
    fo<caret>o();
  }
}

class Base {
  foo() => null;
}
class Method extends Base {
  fooNew(){
  }

  bar(){
    fooNew();
  }
}

class Base {
  fooNew() => null;
}
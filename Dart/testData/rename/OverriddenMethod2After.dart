class Method implements Base {
  fooNew(){
  }

  bar(){
    fooNew();
  }
}

class Base {
  fooNew() => null;
}
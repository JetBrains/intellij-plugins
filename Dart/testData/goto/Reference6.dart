class Reference{
  Bar getBar(){
    return new Bar();
  }
  main(){
    getBar().getBaz().te<caret>st();
  }
}

class Bar {
  Baz getBaz(){
  }
}

class Baz {
  test(){
  }
}
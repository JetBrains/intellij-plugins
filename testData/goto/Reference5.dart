class Reference{
  Bar getBar(){
    return new Bar();
  }
  main(){
    getBar().te<caret>st();
  }
}

class Bar {
  test(){
  }
}
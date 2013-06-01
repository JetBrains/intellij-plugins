class Reference{
  Bar getBar(){
    return new BarImpl();
  }
  main(){
    getBar().te<caret>;
  }
}

interface Bar {
  test1();
  test2();
}
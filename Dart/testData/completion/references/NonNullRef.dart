class Reference{
  Bar getBar(){
    return new BarImpl();
  }
  main(){
    getBar()?.te<caret>;
  }
}

class Bar {
  test1();
  test2();
}

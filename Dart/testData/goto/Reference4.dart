class Reference{
  main(){
    var barOne = new Bar();
    var barTwo = barOne;
    barTwo.te<caret>st();
  }
}

class Bar {
  test(){
  }
}
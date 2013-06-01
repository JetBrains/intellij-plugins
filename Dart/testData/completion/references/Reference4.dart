class Reference{
  main(){
    var barOne = new Bar();
    var barTwo = barOne;
    barTwo.te<caret>;
  }
}

class Bar {
  test1(){
  }
  var test2;
}
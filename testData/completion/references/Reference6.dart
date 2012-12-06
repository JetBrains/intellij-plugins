class Reference{
  Bar bar;
  main(){
    bar.te<caret>
    bar.test2;
  }
}

interface Bar {
  var test1;
  var test2;
}
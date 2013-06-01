class Foo<A, B, C> {
  foo(A a, B b(B param), C c){

  }
}

main(){
  var tmp = new Foo<List<String>, String, int>();
  tmp.foo(null, (str){
    str.len<caret>gth;
  }, -1);
}
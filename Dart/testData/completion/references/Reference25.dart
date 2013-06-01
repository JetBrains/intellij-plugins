main(){
  var l2 = new Foo<Foo>.b<caret>
}

class Foo<T> {
  bar(){}
  T baz(T t) => t;
}
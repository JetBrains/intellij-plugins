class A extends B<C> {}

class C extends D<A> {}

class B<U> {
  foo(){}
}

class D<V> {}

class Generic10 {
  test(){
    var tmp = new A();
    tmp.fo<caret>o();
  }
}
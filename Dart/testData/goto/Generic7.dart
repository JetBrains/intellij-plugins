class A<T> extends B<T> {

}

class B<E> extends C<E> {

}

class C<D> {
  D value;
}

test(A<String> param){
  param.value.len<caret>gth;
}
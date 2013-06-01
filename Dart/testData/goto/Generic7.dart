interface A<T> extends B<T> {

}

interface B<E> extends C<E> {

}

interface C<D> {
  D value;
}

test(A<String> param){
  param.value.len<caret>gth;
}
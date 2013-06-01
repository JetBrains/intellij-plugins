class List<T> {
  T last(){}
  List<T> operator +(T value) => null;
}

main() {
  var l = new List<String>();
  (l + "Hello").last().len<caret>gth;
}
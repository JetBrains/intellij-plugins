main() {
  var a = foo(1, 2);
  var b = foo(10, 20) * 5;
}

foo<caret>(int a, int b) {
  return a + b;
}
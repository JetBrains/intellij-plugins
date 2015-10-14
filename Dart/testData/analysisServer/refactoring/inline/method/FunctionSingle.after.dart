main() {
  var a = foo(1, 2);
  var b = 10<caret> + 20;
}

foo(int a, int b) {
  return a + b;
}
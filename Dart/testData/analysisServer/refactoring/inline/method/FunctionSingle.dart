main() {
  var a = foo(1, 2);
  var b = fo<caret>o(10, 20);
}

foo(int a, int b) {
  return a + b;
}
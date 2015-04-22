class Test {
  String call(int a, double b) => '';
}

main() {
  getNewTest()(<caret>);
}

Test getNewTest() => new Test();
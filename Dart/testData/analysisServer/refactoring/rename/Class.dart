class Test {
  Test() {}
  Test.named() {}
}

class B extends <caret>Test { // in B
}

class C implements Test {
}

/**
 * Has a parameter of type [Test].
 */
main(Test a) {
  print(Test);
  new Test();
  new Test.named();
}
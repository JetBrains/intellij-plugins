class A {
  test() {}
  foo() {}
}

class B extends A {
  <caret>test() {} // B
}

/// Overrides the method [test] from [A].
class C extends A {
  test() {}
  foo() {}
}

class D extends B {
  test() {}
}

/// Defines the method [test], but not in the [A] hierarchy.
class X {
  test() {}
}

main() {
  new A().test();
  new B().test();
  new C().test();
  new D().test();
  new X().test();
}
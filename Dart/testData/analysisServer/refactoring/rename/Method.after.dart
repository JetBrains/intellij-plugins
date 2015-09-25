class A {
  newName() {}
  foo() {}
}

class B extends A {
  <caret>newName() {} // B
}

/// Overrides the method [newName] from [A].
class C extends A {
  newName() {}
  foo() {}
}

class D extends B {
  newName() {}
}

/// Defines the method [test], but not in the [A] hierarchy.
class X {
  test() {}
}

main() {
  new A().newName();
  new B().newName();
  new C().newName();
  new D().newName();
  new X().test();
}
class A {
  newName() {}
  foo() {}
}

class B extends A {
  newName() {} // B
}

class C extends A {
  newName() {}
  foo() {}
}

class D extends B {
  newName() {}
}

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
class A {
  test() {}
  foo() {}
}

class B extends A {
  test() {} // B
}

class C extends A {
  test() {}
  foo() {}
}

class D extends B {
  test() {}
}

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
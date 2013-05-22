class A {
  static get foo => 239;
}

main() {
  A.foo = new Foo();
}

class Foo {}
class A {
  static get f => 239;
}

main() {
  A.f = new Foo();
}

class Foo {}
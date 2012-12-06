class A {
  static set f(Foo value) {
    <caret>
  }

  static get f => 239;
}

main() {
  A.f = new Foo();
}

class Foo {}
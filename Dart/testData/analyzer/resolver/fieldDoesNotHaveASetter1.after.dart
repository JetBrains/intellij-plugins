class A {
  static set foo(Foo value) {
    <caret>
  }

  static get foo => 239;
}

main() {
  A.foo = new Foo();
}

class Foo {}
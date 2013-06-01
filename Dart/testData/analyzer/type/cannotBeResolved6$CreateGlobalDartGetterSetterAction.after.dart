process(x) {}

set unknown(Foo value) {
  <caret>
}

class A {
  foo() {
    unknown = new Foo();
  }
}

class Foo {}
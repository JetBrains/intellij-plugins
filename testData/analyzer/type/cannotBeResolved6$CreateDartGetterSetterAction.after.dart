process(x) {}
class A {
  set unknown(Foo value) {
    <caret>
  }

  foo() {
    unknown = new Foo();
  }
}

class Foo {}
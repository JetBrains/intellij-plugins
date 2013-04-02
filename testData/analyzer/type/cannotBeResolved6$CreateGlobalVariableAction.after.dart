process(x) {}

Foo unknown;

class A {
  foo() {
    unknown = new Foo();
  }
}

class Foo {}
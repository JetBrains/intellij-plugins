// this import is here to force analyzer look there and find potential name() method reference
import 'dart:html';

class A {
  name() {}
  foo() {}
}

class B extends A {
  <caret>name() {} // B
}

/// Overrides the method [name] from [A].
class C extends A {
  name() {}
  foo() {}
}

class D extends B {
  name() {}
}

/// Defines the method [name], but not in the [A] hierarchy.
class X {
  name() {}
}

main() {
  new A().name();
  new B().name();
  new C().name();
  new D().name();
  new X().name();
}
// this import is here to force analyzer look there and find potential name() method reference
import 'dart:html';

class A {
  newName() {}
  foo() {}
}

class B extends A {
  <caret>newName() {} // B
}

/// Overrides the method [newName] from [A].
class C extends A {
  newName() {}
  foo() {}
}

class D extends B {
  newName() {}
}

/// Defines the method [name], but not in the [A] hierarchy.
class X {
  name() {}
}

main() {
  new A().newName();
  new B().newName();
  new C().newName();
  new D().newName();
  new X().name();
}
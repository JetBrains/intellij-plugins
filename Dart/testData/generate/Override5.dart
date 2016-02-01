class A {
  publicMethodInA() {}
  _privateMethodInA() {}
}

class B extends A {
  var someField;
  publicMethodInB() {}
  _privateMethodInB() {}
}

class C extends B {
  <caret>
}

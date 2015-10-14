class A {
  mmm() {}
}
class B extends A {
  <caret>mmm() {}
}
class C extends B {
  mmm() {}
}
class D extends C {
  mmm() {}
}
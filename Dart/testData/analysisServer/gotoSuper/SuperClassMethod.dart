class A {
  mmm() {}
}
class B extends A {
  mmm() {}
}
class C extends B {
  mm<caret>m() {}
}
class D extends C {
  mmm() {}
}
package com.jetbrains.lang.dart.ide.hierarchy.method;

public class Temp {
}

interface I1 {
  void foo();
}

interface I2 {
  void foo();
}

abstract class A implements I1 {
}

class B extends A implements I2 {
  public void foo() {
    new D().foo(); // MH from this foo() is rooted at B
  }
}

class C extends B {
}

class D extends B {
}

class E extends C {
  public void foo() {
  }
}

class F extends E {
}

abstract class X implements I1 {

}

class Y {
  void f(X a) {
    a.foo(); // MH from this foo() is rooted at I1
  }
}
package AAA {
  class A {
    public var field;
  }
}

package BBB {
  import AAA.A;

  class C extends A {
    function aaa() {
      fi<ref>eld = 1;
    }
  }
}

class B {
  var field;
}

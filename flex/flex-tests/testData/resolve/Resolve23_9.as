package AAA {
  class A {
    public var field;
  }
}

package BBB {
  import AAA.A;

  class C extends A {
    function aaa() {
      fi<caret>eld = 1;
    }
  }
}

class B {
  var field;
}

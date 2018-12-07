package BBB {
  class E {
    protected var field;
  }
}

package BBB2 {
  import BBB.E;
  class E2 extends E {
  }
}

package AAA {
  import BBB2.E2;

  class C extends E2 {
  }

  class A extends C {
    var field;

    function aaa() {
      super.fi<caret>eld
    }
  }

  class B {
    var field;
  }
}

package BBB {
  import AAA.B;

  class C extends B {
    function aaa() {
      fi<caret>eld = 1;
    }
  }
}

class B {
  var field;
}

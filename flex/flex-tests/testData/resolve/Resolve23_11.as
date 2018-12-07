package AAA {
  public class B {
    function B() {}
  }
}
package BBB {
  import AAA.B;

  class C extends <caret>B {
  }
}

class B2 {
  var field;
}

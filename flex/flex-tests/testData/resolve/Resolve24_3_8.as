package foo2 {
  dynamic class ZZZ {
      var xxx;
  }
  class CCC extends ZZZ {
    function yyy() {
        x<caret>xx
    }
  }
}

package bar {
  class XXX {
      var xxx;
      function XXX() {
          this.xxx = 1;
      }
  }
}
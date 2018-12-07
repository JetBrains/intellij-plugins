package foo {

  dynamic class CCC {
    var xxx;
    function yyy() {
        xx<caret>x
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
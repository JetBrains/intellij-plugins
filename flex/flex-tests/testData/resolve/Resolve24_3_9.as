package foo {
  dynamic class CCC {
    function yyy() {
        this.v<caret>vv
    }
    function zzz() {
        this.vvv = 1;
    }
  }
}

class Bar {
    function vvv() {}
}
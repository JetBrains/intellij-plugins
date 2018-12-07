import zzz.C;

class B {
  var aaa;
}

package zzz {
  import xxx.A;
  class C extends A {

  }
}

z.aaa = 1;

function fff() {
  var x:C;
  x.a<caret>aa = 1;
}

package xxx {
  class A {
    var aaa;
    function A() {}
  }
}

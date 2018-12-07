import xxx.A;

package xxx {
  class A {
    static var aaa;
  }
}

class B {
  var aaa;
}

package zzz {
  class C extends A {

  }
}

z.aaa = 1;

import zzz.C;

function fff() {
  A.a<caret>aa = 1;
}

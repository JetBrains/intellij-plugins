import mypackage.B;
import mypackage.myns;
import mypackage3.mynestedpackage.MyClass;
import yetanotherpackage.AAA;

var foo:mypackage3.mynestedpackage.MyClass;
var bar:<error>mypackage2</error>.MyLibClass;

package <error>mypackage</error> {
  public namespace <error>myns</error>;
  public class <error>B</error> {
    public static function getInstance(p):B {}
  }
}

package <error>mypackage3.mynestedpackage</error> {
  public class <error>MyClass</error> extends <error>MyLibClass</error> {}
}

use namespace myns;

class A {
  var ccc = new B();
  function aaa(p = null) {
    <error>bbb</error>();
    this.<error>bbb</error>();
    var c = aaa();
    var c3 = this.aaa();
    var c2 = ccc;
    var c4 = this.ccc;
    ccc = <error>eee</error>;
    this.ccc = this.<error>eee</error>;
    var ccc4 = new B();
    var ccc4_2 = mypackage.B.getInstance(1);
    aaa(ccc4);

    if (ccc4 is B) {
      var ccc5 = ccc4 as B;
      var ccc6 = B.getInstance(1);
      myfun(ccc5)
      myfun(ccc4_2)

      ccc.getInstance(B(ccc5));
      B.getInstance(B(ccc5));
      var sss:yetanotherpackage.AAA;
      sss.field = 1;
    }
  }
  
  myns function myfun(p) {}
}

var a;
a.bbb = function() {}
a.eee = 1;

package <error>yetanotherpackage</error> {
  class <error>BBB</error> extends AAA {
    function BBB() {
      field = AAA(this).field;
    }
  }
}

class Foo {
  [Event(type="<error>Foo</error>")]
  [<weak_warning>Event2</weak_warning>]
  var x;
}

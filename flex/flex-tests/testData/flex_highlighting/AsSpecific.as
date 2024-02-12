import mypackage.B;
import mypackage.myns;
import mypackage3.mynestedpackage.MyClass;
import yetanotherpackage.AAA;

var foo:mypackage3.mynestedpackage.MyClass;
var bar:<error descr="Unresolved variable or type mypackage2">mypackage2</error>.MyLibClass;

<error descr="Package should be first statement in file">package</error> <error descr="Package name 'mypackage' does not correspond to file path ''">mypackage</error> {
  public namespace <error descr="More than one externally visible symbol defined in file"><error descr="Namespace 'myns' should be defined in file 'myns.as'">myns</error></error>;
  public class <error descr="Class 'B' should be defined in file 'B.as'"><error descr="More than one externally visible symbol defined in file">B</error></error> {
    public static function getInstance(p):B {}
  }
}

<error descr="Package should be first statement in file">package</error> <error descr="Package name 'mypackage3.mynestedpackage' does not correspond to file path ''">mypackage3.mynestedpackage</error> {
  public class <error descr="Class 'MyClass' should be defined in file 'MyClass.as'">MyClass</error> extends <error descr="Unresolved type MyLibClass">MyLibClass</error> {}
}

use namespace myns;

class A {
  var ccc = new B();
  function aaa(p = null) {
    <error descr="Unresolved function or method bbb()">bbb</error>();
    this.<error descr="Unresolved function or method bbb()">bbb</error>();
    var c = aaa();
    var c3 = this.aaa();
    var c2 = ccc;
    var c4 = this.ccc;
    ccc = <error descr="Unresolved variable or type eee">eee</error>;
    this.ccc = this.<error descr="Unresolved variable eee">eee</error>;
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

<error descr="Package should be first statement in file">package</error> <error descr="Package name 'yetanotherpackage' does not correspond to file path ''">yetanotherpackage</error> {
  class <error descr="Class 'BBB' should be defined in file 'BBB.as'">BBB</error> extends AAA {
    function BBB() {
      field = AAA(this).field;
    }
  }
}

class Foo {
  [Event(type="<error descr="Expected class flash.events.Event or descendant">Foo</error>")]
  [<weak_warning descr="Unknown metadata attribute used">Event2</weak_warning>]
  var x;
}

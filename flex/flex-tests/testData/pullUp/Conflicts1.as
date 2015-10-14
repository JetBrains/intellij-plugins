package foo {
public class Super {
    function foo();
}
}
package bar {
import foo.Super;

public class Sub extends Super {
    public var fooVar = st4;
    public var fooVar2 : Aux2;

    public function foo(p:Aux) {
        bar();
        v = 0;
        v2 = 0;
        var p : FileLocal;
        var z = st1;
        var z2 = st2;
        var z3 = st3();
    }
    private function bar();

    protected var v;

    public var v2;

    private static var st1;
    protected static var st2;
    protected static var st4;
    public static function st3() {};
}

class Aux {}
class Aux2 {}
}

class FileLocal{

}
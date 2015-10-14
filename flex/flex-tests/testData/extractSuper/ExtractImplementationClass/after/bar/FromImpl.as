package bar {
import com.*;

import zzz.Abc;

public class FromImpl implements INotMoved, From {
    var v : Abc;

    public function movedMethod() {
    }

    public function notMovedMethod() {
    }

    public function notMovedMethod2() {
    }

    public function get movedProp():int {
    }

    public function set movedProp(p:int) {
    }

    public function get movedProp2():int {
    }

    public function set notMovedProp(p:String) {
    }

    public static function staticFunc() {}

    public static function get staticProp():int { return 0; }

    public static const ID = "abc";

    function foo() {
        var v = foo2();
    }

    function foo2() {
        var t = foo();
    }
}
}

import zzz.Abc;

class Aux {
    var b : Abc;
}

function foo() {

}

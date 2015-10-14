package com {
import zz.MyInt1;

public class From extends FromBase implements MyInt1, MyInt2 {
    public var v1 : int;
    var v2  = foo2();

    /**
     * foo1 docs
     * @param p
     */
    public function foo1(p:Param) {
        foo2();
    }

    public static function foo2() {
        foo1();
    }
}
}

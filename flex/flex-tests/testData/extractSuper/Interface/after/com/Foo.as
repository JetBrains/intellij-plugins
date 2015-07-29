package com {
import bar.IFoo;

public class Foo implements MyInt2, IFoo {
    /**
     * blabla
     * @param p
     */
    public function moved1(p:SomeClass) {

    }

    /**
     * zzzz
     * @param v
     * @param p
     */
    public function moved2(v:String = "abc", ...p) {
    }

    public function moved3() {
    }

    public function moved4() {
    }

    public function moved5() {
    }

    public function notMoved() {}

    public static function staticFunc():void {
    }

    public static function get staticProp():int {
        return 0;
    }

    public static const ID = "abc";

}
}


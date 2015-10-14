package com {
public class Foo implements MyInt, MyInt2 {
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

    protected function moved3() {}
    private function moved4() {}
    function moved5() {}

    public function notMoved() {}

    public static function staticFunc():void {
    }

    public static function get staticProp():int {
        return 0;
    }

    public static const ID = "abc";

}
}


package foo {
import bar.Base;

public class Sub extends Base {
    /**
     * bar docs
     */

    private function bar() {
        zz();
        super.basefunc();
        var a : Aux1;
    }

    /**
     * zz docs
     */
    private function zz() {
        st();
        bar();
        this.bar();
        new Sub().bar2();
    }

    function bar2() {bar();}

    public static function st() {}

    public static function st2() {}
    public static const st3;
}
}

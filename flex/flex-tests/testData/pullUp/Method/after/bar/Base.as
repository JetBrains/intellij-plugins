package bar {
import foo.Aux1;
import foo.Sub;

public class Base {
    public static const st3;

    protected function basefunc() {}

    /**
     * bar docs
     */

    protected function bar() {
        zz();
        basefunc();
        var a:Aux1;
    }

    /**
     * zz docs
     */
    private function zz() {
        Sub.st();
        bar();
        this.bar();
        new Sub().bar2();
    }

    public static function st2() {
    }
}
}

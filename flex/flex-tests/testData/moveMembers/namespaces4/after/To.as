package {
use namespace MyNs2;

public class To {

    public static const ZZ;

    public static function foo() {
        bar();
    }

    public static function bar() {
        ZZ = 0;
        From.ZZ2 = 0;
    }
}
}
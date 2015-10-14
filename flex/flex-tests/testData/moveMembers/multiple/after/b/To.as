package b {
public class To {
    private static var field = method1();

    public static const c;

    public static const c2;

    public function To() {
    }

    private static function method1() {
        field = 0;
        method2();
    }

    private static function method2() {
        method1();
    }

    private static function method3() {
        method3();
    }
}
}
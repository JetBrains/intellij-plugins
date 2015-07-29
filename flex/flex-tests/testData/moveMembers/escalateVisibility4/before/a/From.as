package a {
import b.To;

public class From extends To {
    static private var privateField;
    static var internalField1;
    static internal var internalField2;
    protected static var protectedField;
    public static var publicField;

    private static function privateMethod() {
    }
    static function internalMethod1() {
    }
    internal static function internalMethod2() {
    }
    protected static function protectedMethod() {
    }
    public static function publicMethod() {
    }


    public function From() {
        privateMethod();
        internalMethod1();
        internalMethod2();
        publicMethod();
        protectedMethod();

        privateField = 0;
        internalField1 = 0;
        internalField2 = 0;
        publicField = 0;
        protectedField = 0;
    }
}
}
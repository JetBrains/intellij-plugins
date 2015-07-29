package b {
public class To {

    public static function foo() {
        bar();
        var t:SomeClass;
    }

    public static function bar() {
        foo();
    }
}
}
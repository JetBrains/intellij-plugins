package a {

public class From {
    public static function foo() {
        From.bar();
        var t : SomeClass;
    }
    public static function bar() {
        From.foo();
    }
    public static function zzz() {
        bar();
    }
}
}
package foo {
public class From {

    public static var v = func();

    public static function zzz() {
        if (func() != null) {
            var t = func();
        }
    }

    public static function func() {
        v = "abc";
        zzz();
    }
}
}
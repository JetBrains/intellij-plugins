package foo {
public class From {

    public static var v = To.func();

    public static function zzz() {
        if (To.func() != null) {
            var t = To.func();
        }
    }

}
}
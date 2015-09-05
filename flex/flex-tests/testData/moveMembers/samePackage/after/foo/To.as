package foo {
public class To {

    private var bb = func();
    
    public function To() {
        if (func() > 5) {
            func();
        }
    }

    public static function func() {
        From.v = "abc";
        From.zzz();
    }
}
}
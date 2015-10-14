package {
import mypackage.Alert;

public class Sub2 extends Super {

    /**
     * tttt
     */
    private var t = 0;

    /**
     * and v
     */
    public static var v;

    /**
     * foo
     * @return
     */
    public function foo():Alert {
        bar();
        u = 0;
        u2 = 0;
    }

    /**
     * just bar
     */
    protected function bar() {
        bar();
        t = 0;
    }

    /**
     * this is uu
     */
    public static function uu() {
    }
}
}

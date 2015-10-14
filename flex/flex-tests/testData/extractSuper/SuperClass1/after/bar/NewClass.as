package bar {
import com.FromBase;

import zz.MyInt1;

public class NewClass extends FromBase implements MyInt1 {
    public var v1:int;

    var v2 = foo2();

    public function NewClass() {
    }

    /**
     * foo1 docs
     * @param p
     */
    public function foo1(p:Param) {
        foo2();
    }

    public static function foo2() {
        foo1();
    }
}
}

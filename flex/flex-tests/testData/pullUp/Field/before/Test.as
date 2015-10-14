package foo {

public class Super {


}
}

package bar {
import foo.Super;
public class Sub extends Super {

    /**
     * ppp
     */
    private var p = 0;

    /**
     * ddd
     */
    private var d : String = foo();

    public static function foo():String {
        return "abc";
    }


    private static var e : Aux;
}

public class Aux{}

}
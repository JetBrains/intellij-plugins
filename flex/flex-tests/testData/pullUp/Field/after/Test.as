package foo {
import bar.Aux;
import bar.Sub;

public class Super {


    /**
     * ppp
     */
    private var p = 0;
    /**
     * ddd
     */
    private var d:String = Sub.foo();
    private static var e:Aux;
}
}

package bar {
import foo.Super;

public class Sub extends Super {


    public static function foo():String {
        return "abc";
    }


}

public class Aux{}

}
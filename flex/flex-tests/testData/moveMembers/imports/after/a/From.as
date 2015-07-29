package a {
import b.To;

public class From {

    public static var fromVar;
    var before;
    /*after*/

    public static function fromA(u:Param):Result {
        return To.foo(u);
    }
}
}
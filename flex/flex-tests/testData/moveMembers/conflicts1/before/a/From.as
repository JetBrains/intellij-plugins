package a {
public class From {
    private static var foo = move1();
    //private static var foo2 = From.move1(); until IDEADEV-40456 is fixed

    protected static var bar = move2;

    internal static var zzz = move3;

    protected static const move2 = 5;

    internal static const move3 = 5;

    private static function move1() {
        foo = 0;
        From.foo = 0;
        bar = 0;
        zzz = 0;
        var p : Aux;
    }

}
}

class Aux {

}
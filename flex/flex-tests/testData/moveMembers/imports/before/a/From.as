package a {
public class From {

    public static var fromVar;
    var before;
    public static function foo(u:Param):Result {
        fromVar = 0;
        var r : Result;
        globalFunc(globalVar);

        new Abcde();
        new Abcde().zzz();
        var v : Abcde;
        v.zzz();
        Abcde.sss();
        return fromA(u);
    }
    /*after*/

    public static function fromA(u:Param):Result {
        return foo(u);
    }
}
}
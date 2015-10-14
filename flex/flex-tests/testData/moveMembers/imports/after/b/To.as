package b {
import a.Abcde;
import a.From;
import a.Param;
import a.Result;
import a.globalFunc;
import a.globalVar;

public class To {
    public function To() {
    }

    public static function foo(u:Param):Result {
        From.fromVar = 0;
        var r:Result;
        globalFunc(globalVar);

        new Abcde();
        new Abcde().zzz();
        var v:Abcde;
        v.zzz();
        Abcde.sss();
        return From.fromA(u);
    }
}
}
package {
public class <warning descr="Unused class UnusedGlobalSymbols5">UnusedGlobalSymbols5</warning> {

    public static const CONST1:String = "";
    public static const <warning descr="Unused constant CONST2">CONST2</warning>:String = UnusedGlobalSymbols5.CONST1;

    public function UnusedGlobalSymbols5(info:String) {
    }

    public function <warning descr="Unused method foo">foo</warning>():UnusedGlobalSymbols5 {
        if (true) foo();
        return new UnusedGlobalSymbols5("");
    }

}
}

package {

public class ImplicitCoercionToString {
    public function ImplicitCoercionToString() {
        var s:String;
        var b:Object;
        var c:Number;

        s = (condition() ? <error descr="Expression type Object is implicitly coerced to unrelated type String">b</error> : <error descr="Expression type Number is implicitly coerced to unrelated type String">c</error>); // red
        s = "" + (((condition() ? b : condition() ? 1 : false)));
        s = (condition() ? b : c) + "";
        s = "" + (condition() ? 1 : 2.5) ;
        s = "" + (condition() ? true : c);
    }

    private static function condition():Boolean {
        return true;
    }
}
}

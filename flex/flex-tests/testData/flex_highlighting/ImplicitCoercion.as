package {

import flash.utils.Dictionary

public class ImplicitCoercion {
    public function ImplicitCoercion() {
        var s:String;
        var b:Object;
        var c:Number;
        var d:Dictionary;
        var a:Array;

        s = (condition() ? <error descr="Expression type Object is implicitly coerced to unrelated type String">b</error> : <error descr="Expression type Number is implicitly coerced to unrelated type String">c</error>); // red
        s = "" + (((condition() ? b : condition() ? 1 : false)));
        s = (condition() ? b : c) + "";
        s = "" + (condition() ? 1 : 2.5) ;
        s = "" + (condition() ? true : c);

        b = a[condition() ? <error descr="Expression type String is implicitly coerced to unrelated type int | uint">s</error> : <error descr="Expression type Object is implicitly coerced to unrelated type int | uint">b</error>];
        b = d[condition() ? s : b];
        b = d[condition() ? true : false ? [] : {}];
    }

    private static function condition():Boolean {
        return true;
    }
}
}

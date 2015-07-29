package {

public class NewObjectClassFunction {
    public static function foo(P:Object):void {
        var S:String;
        var B:Boolean;
        var C:Class;
        var N:Number;
        var I:int;
        var O:Object;
        var F:Function;

        new <error>S</error>();
        new <error>B</error>();
        new <error>N</error>();
        new <error>I</error>();

        new C();
        new O();
        new P();
        new F();
    }
}
}

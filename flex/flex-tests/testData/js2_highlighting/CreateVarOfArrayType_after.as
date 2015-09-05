package {
public class CreateVarOfArrayType {
    [ArrayElementType("flash.events.EventDispatcher")]
    public var arr: Array;

    private function foo():void {
        var x:Array;
        x = arr;
    }
}
}
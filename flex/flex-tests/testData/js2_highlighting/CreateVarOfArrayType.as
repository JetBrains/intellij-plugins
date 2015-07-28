package {
public class CreateVarOfArrayType {
    [ArrayElementType("flash.events.EventDispatcher")]
    public var arr: Array;

    private function foo():void {
        <error>x</error> = arr;
    }
}
}
import flash.events.Event;
import flash.events.EventDispatcher;

class Foo extends EventDispatcher {
    function bar() {
        addEventListener("xxx", function (event:Event):void {});
        addEventListener("xxx", function (i: int, s: String):void {});
        addEventListener("xxx", function (i: int):void {});
    }
}
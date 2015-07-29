import flash.events.Event;
import flash.events.EventDispatcher;

class Foo extends EventDispatcher {
    function bar(event:Event) {
        addEventListener("xxx", bar);
    }
}
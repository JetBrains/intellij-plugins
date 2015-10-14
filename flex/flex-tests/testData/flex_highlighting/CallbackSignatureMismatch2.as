import flash.events.EventDispatcher;
import flash.events.MouseEvent;
class C extends EventDispatcher {
    function foo(event:MouseEvent) {
        addEventListener(MouseEvent.CLICK, foo);
    }
}
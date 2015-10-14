package {
import flash.events.EventDispatcher;
import flash.events.MouseEvent;

[Event(name, type="flash.events.MouseEvent")]
public class InvalidAttribute extends EventDispatcher {

    public function Test() {
        addEventListener(MouseEvent.CLICK, foo)
    }

    private function foo(event:MouseEvent):void {
    }
}

}

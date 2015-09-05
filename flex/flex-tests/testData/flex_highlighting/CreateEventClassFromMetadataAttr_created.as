package foo {
import flash.events.Event;

public class MyEvent extends Event {
    public function MyEvent(type:String, bubbles:Boolean = false, cancelable:Boolean = false) {
        super(type, bubbles, cancelable);
    }
}
}

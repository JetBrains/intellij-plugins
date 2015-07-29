import flash.events.*;

[Event(name="xxx", type = "flash.events.KeyboardEvent")]
class Foo extends EventDispatcher {
    function bar(event:KeyboardEvent) {
        addEventListener(KeyboardEvent.TYPED, bar);
    }
}
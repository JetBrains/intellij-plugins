import flash.events.*;

[Event(name="xxx", type = "flash.events.KeyboardEvent")]
class Foo extends EventDispatcher {
    function bar() {
        addEventListener(KeyboardEvent.TYPED, <weak_warning>bar</weak_warning>);
    }
}
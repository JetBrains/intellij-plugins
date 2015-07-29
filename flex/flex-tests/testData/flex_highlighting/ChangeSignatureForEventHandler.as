import flash.events.EventDispatcher;

class Foo extends EventDispatcher {
    function bar() {
        addEventListener("xxx", <weak_warning>bar</weak_warning>);
    }
}
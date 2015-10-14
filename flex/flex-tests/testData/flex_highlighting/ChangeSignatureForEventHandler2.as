import flash.events.EventDispatcher;

class Foo extends EventDispatcher {
    function bar() {
        addEventListener("xxx", function <weak_warning>()</weak_warning>:void {});
        addEventListener("xxx", function <weak_warning>(i: int, s: String)</weak_warning>:void {});
        addEventListener("xxx", function (<weak_warning>i: int</weak_warning>):void {});
    }
}
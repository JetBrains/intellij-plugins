package {
import flash.events.EventDispatcher;
import flash.events.MouseEvent;

public class Foo extends EventDispatcher {
      function ab<caret>c() {}

      override public function dispatchEvent(event: Event): Boolean {
            abc();
        }

      function bar() {
          abc();
      }

      function zzzzz() {
          var listener: Function = function(e: MouseEvent): void {
              abc();
            };
          addEventListener(MouseEvent.CLICK, onClick);
      }

      function onClick(e: MouseEvent) : void {
          abc();
      }
  }
}

class Zzz extends LibraryClass {
    override public function foo() {
        new Foo().abc();
    }
}

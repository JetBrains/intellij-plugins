package {
import flash.events.Event;
public class NoCreateFieldInSdkClass {
  private function foo() {
    var v : Event;
    v.<error>f<caret>oo</error> = 0;
  }
}
}

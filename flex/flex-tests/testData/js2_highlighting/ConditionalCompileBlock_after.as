package {
import flash.events.KeyboardEvent;

public class ConditionalCompileBlock {
  public function ConditionalCompileBlock() {

    CONFIG::bar {
      import flash.events.Event;

      CONFIG::foo {
        import mx.events.FlexEvent;
        var a : KeyboardEvent;
        a = null;
      }

    }
  }
}
}
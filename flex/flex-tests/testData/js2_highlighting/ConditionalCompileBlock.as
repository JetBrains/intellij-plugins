package {
public class ConditionalCompileBlock {
  public function ConditionalCompileBlock() {

    <error>CONFIG</error>::bar {
      import flash.events.Event;

      <error>CONFIG</error>::foo {
        import mx.events.FlexEvent;
        var a : <error descr="Unresolved type KeyboardEvent">Keyboard<caret>Event</error>;
        a = null;
      }

    }
  }
}
}
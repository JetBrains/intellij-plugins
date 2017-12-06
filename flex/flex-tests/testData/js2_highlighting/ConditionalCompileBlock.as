package {
public class ConditionalCompileBlock {
  public function ConditionalCompileBlock() {

    <error>CONFIG</error>::<error>bar</error> {
      import flash.events.Event;

      <error>CONFIG</error>::<error>foo</error> {
        import mx.events.FlexEvent;
        var a : <error descr="Unresolved type KeyboardEvent">Keyboard<caret>Event</error>;
        a = null;
      }

    }
  }
}
}
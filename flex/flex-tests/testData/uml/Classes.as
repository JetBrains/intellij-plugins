package {
<caret expected="Bar">
public cla<caret expected="Bar">ss Ba<caret expected="Bar">r {
    <caret expected="Bar">

    public static <caret expected="Bar">function bar():Number {
        <caret expected="Bar">
    }
}

public class Fo<caret expected="Foo">o extends Ba<caret expected="Bar">r {
      <caret expected="Foo">

      function fo<caret expected="Foo">o():String {
          <caret expected="Foo">
      }

      private function get proprw():Boolean {
      }

      private function get propr():Boolean {
      }

      private function set proprw(p:Boolean):void {
      }

      private function set propw(p:Boolean):void {
      }

      var v : int = 0;
      const t : String;
}
 <caret expected="Bar">
}
<caret expected="Bar">

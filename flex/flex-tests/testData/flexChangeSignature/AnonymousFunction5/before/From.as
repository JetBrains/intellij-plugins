package {

public class From {
  private static var say:Function = function(msg:String):void {
    trace(msg);
  }

  function usage1() { // propagate
    s<caret>ay("Hello");
  }

  function usage2() {
    say("Goodbye");
  }

}
}

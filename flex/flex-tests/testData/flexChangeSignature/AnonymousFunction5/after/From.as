package {

public class From {
  public static var sayLoud:Function = function (message:String, loud:Boolean = true):void {
    trace(message);
  }

  function usage1(loud:Boolean = true) { // propagate
    sayLoud("Hello", loud);
  }

  function usage2() {
    sayLoud("Goodbye", false);
  }

}
}

package org.jetbrains.util {
public class Runnable {
  private var handler:Function;
  private var parameters:Array;

  public function Runnable(handler:Function, parameters:Array) {
    this.handler = handler;
    if (parameters != null && parameters.length > 0) {
      this.parameters = parameters;
    }
  }

  public function run():void {
    if (parameters == null) {
      handler();
    }
    else {
      handler.apply(null, parameters);
    }
  }
}
}
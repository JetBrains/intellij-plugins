package org.jetbrains.util {
public class ActionCallback {
  private var done:ExecutionCallback;
  private const rejected:ExecutionCallback = new ExecutionCallback();

  public function ActionCallback(countToDone:int = 1) {
    done = new ExecutionCallback(countToDone);
  }

  public function doWhenDone(listener:Function, ...parameters):ActionCallback {
    done.doWhenExecuted(listener, parameters);
    return this;
  }

  public function doWhenRejected(listener:Function, ...parameters):ActionCallback {
    rejected.doWhenExecuted(listener, parameters);
    return this;
  }

  public function doWhenProcessed(listener:Function):ActionCallback {
    doWhenDone(listener);
    doWhenRejected(listener);
    return this;
  }

  public function setDone():void {
    done.setExecuted();
  }

  public function setRejected():void {
    rejected.setExecuted();
  }

  public function get isDone():Boolean {
    return done.isExecuted;
  }

  public function get isRejected():Boolean {
    return rejected.isExecuted;
  }
}
}
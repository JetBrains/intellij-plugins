package org.jetbrains.util {
public class ExecutionCallback {
  private var firstRunnable:MyRunnable;

  private var currentCount:int;
  private var countToExecution:int = 1;

  public function ExecutionCallback(countToExecution:int = 1) {
    this.countToExecution = countToExecution;
  }

  public function get isExecuted():Boolean {
    return currentCount >= countToExecution;
  }

  public function doWhenExecuted(listener:Function, parameters:Array):void {
    const hasParameters:Boolean = parameters != null && parameters.length > 0;
    if (isExecuted) {
      if (hasParameters) {
        listener.apply(null, parameters);
      }
      else {
        listener();
      }
    }
    else {
      var runnable:MyRunnable = new MyRunnable(listener, parameters);
      var last:MyRunnable = firstRunnable;
      if (last == null) {
        firstRunnable = runnable;
      }
      else {
        while (last.next != null) {
          last = last.next;
        }
        last.next = runnable;
      }
    }
  }

  public function setExecuted():void {
    currentCount++;

    if (isExecuted) {
      var runnable:MyRunnable = firstRunnable;
      firstRunnable = null;
      while (runnable != null) {
        runnable.run();
        runnable = runnable.next;
      }
    }
  }
}
}

import org.jetbrains.util.Runnable;

final class MyRunnable extends Runnable {
  internal var next:MyRunnable;

  public function MyRunnable(handler:Function, parameters:Array) {
    super(handler, parameters);
  }
}
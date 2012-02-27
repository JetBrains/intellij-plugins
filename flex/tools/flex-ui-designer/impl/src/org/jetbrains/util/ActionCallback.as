package org.jetbrains.util {
public class ActionCallback {
  private const done:ExecutionCallback = new ExecutionCallback();
  private const rejected:ExecutionCallback = new ExecutionCallback();

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

import org.osflash.signals.ISlot;
import org.osflash.signals.OnceSignal;

final class ExecutionCallback {
  private var executed:Boolean;
  private var signal:OnceSignal;

  public function get isExecuted():Boolean {
    return executed;
  }

  public function doWhenExecuted(listener:Function, parameters:Array):void {
    const hasParameters:Boolean = parameters != null && parameters.length > 0;
    if (executed) {
      if (hasParameters) {
        listener.apply(null, parameters);
      }
      else {
        listener();
      }
    }
    else {
      if (signal == null) {
        signal = new OnceSignal();
      }
      var slot:ISlot = signal.addOnce(listener);
      if (hasParameters) {
        slot.params = parameters;
      }
    }
  }

  public function setExecuted():void {
    executed = true;
    if (signal != null) {
      signal.dispatch();
    }
  }
}

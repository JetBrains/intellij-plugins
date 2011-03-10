package com.intellij.flex.uiDesigner.flex.states {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.errors.IllegalOperationError;

import mx.core.ITransientDeferredInstance;

public final class PermanentArrayOfDeferredInstanceFromBytes implements ITransientDeferredInstance {
  public var array:Array;
  private var processed:Boolean;
  public var context:DeferredInstanceFromBytesContext;

  public function getInstance():Object {
    if (!processed) {
      processed = true;
      for (var i:int = 0, n:int = array.length; i < n; i++) {
        array[i] = DeferredInstanceFromBytes(array[i]).create(context);
      }
    }
    
    return array;
  }

  public function reset():void {
    throw new IllegalOperationError();
  }
}
}

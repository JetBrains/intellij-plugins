package com.intellij.flex.uiDesigner.flex.states {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.errors.IllegalOperationError;

import mx.core.ITransientDeferredInstance;

public final class PermanentArrayOfDeferredInstanceFromBytes implements ITransientDeferredInstance {
  private var array:Array;
  private var processed:Boolean;
  private var context:DeferredInstanceFromBytesContext;
  
  public function PermanentArrayOfDeferredInstanceFromBytes(array:Array, context:DeferredInstanceFromBytesContext) {
    this.array = array;
    this.context = context;
  }

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

  public function get __array():Array {
    return array;
  }
}
}

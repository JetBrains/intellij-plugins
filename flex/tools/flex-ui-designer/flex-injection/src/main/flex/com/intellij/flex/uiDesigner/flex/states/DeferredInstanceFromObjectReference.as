package com.intellij.flex.uiDesigner.flex.states {
import flash.errors.IllegalOperationError;

import mx.core.ITransientDeferredInstance;

public class DeferredInstanceFromObjectReference implements ITransientDeferredInstance {
  public var reference:int;
  public var deferredParentInstance:DeferredInstanceFromBytes;
  
  public function getInstance():Object {
    return deferredParentInstance.getReferredChild(reference);
  }

  public function reset():void {
    throw new IllegalOperationError("must be called on parent");
  }
}
}

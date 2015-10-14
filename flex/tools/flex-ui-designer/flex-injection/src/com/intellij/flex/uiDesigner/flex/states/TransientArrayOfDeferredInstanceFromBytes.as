package com.intellij.flex.uiDesigner.flex.states {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import mx.core.ITransientDeferredInstance;

public final class TransientArrayOfDeferredInstanceFromBytes implements ITransientDeferredInstance {
  private var deferredInstances:Vector.<DeferredInstanceFromBytes>;
  public var instances:Array;
  
  private var context:DeferredInstanceFromBytesContext;

  public function TransientArrayOfDeferredInstanceFromBytes(deferredInstances:Vector.<DeferredInstanceFromBytes>, context:DeferredInstanceFromBytesContext) {
    this.deferredInstances = deferredInstances;
    this.context = context;
  }

  public function getInstance():Object {
    if (instances == null || instances.length == 0) {
      var n:int = deferredInstances.length;
      if (instances == null) {
        instances = new Array(n);
      }
      else {
        instances.length = n;
      }
      
      for (var i:int = 0; i < n; i++) {
        instances[i] = deferredInstances[i].create(context);
      }
    }
    
    return instances;
  }

  public function reset():void {
    if (instances != null && instances.length != 0) {
      for each (var deferredInstance:DeferredInstanceFromBytes in deferredInstances) {
        deferredInstance.reset();
      }
      
      instances.length = 0;
    }
  }
  
  public function get __deferredInstances():Vector.<DeferredInstanceFromBytes> {
    return deferredInstances;
  }
}
}

package com.intellij.flex.uiDesigner.flex.states {
import com.intellij.flex.uiDesigner.flex.BindingTarget;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.errors.IllegalOperationError;
import flash.utils.ByteArray;

[Abstract]
public class DeferredInstanceFromBytesBase {
  private var bytes:ByteArray;
  private var objectTable:Vector.<Object>;

  protected var instance:Object;

  private var bindingTargets:Vector.<BindingTarget>;

  public function DeferredInstanceFromBytesBase(bytes:ByteArray) {
    this.bytes = bytes;
  }

  public function create(context:DeferredInstanceFromBytesContext):Object {
    if (instance == null) {
      instance = context.reader.readDeferredInstanceFromBytes(bytes, context);
      objectTable = context.reader.getObjectTableForDeferredInstanceFromBytes();

      executeBindinds();
    }

    return instance;
  }

  private function executeBindinds():void {
    if (bindingTargets != null) {
      for each (var bindingTarget:BindingTarget in bindingTargets) {
        bindingTarget.execute(instance);
      }
    }
  }

  public function getInstanceIfCreatedOrRegisterBinding(bindingTarget:BindingTarget):Object {
    if (instance == null) {
      if (bindingTargets == null) {
        bindingTargets = new Vector.<BindingTarget>();
      }

      bindingTargets.push(bindingTarget);
    }

    return instance;
  }

  public function getInstance():Object {
    if (instance == null) {
      throw new IllegalOperationError("must be created before this moment");
    }

    return instance;
  }

  public function reset():void {
    instance = null;
    objectTable = null;

    executeBindinds();
  }

  internal function getReferredChild(reference:int):Object {
    var o:Object;
    if ((o = objectTable[reference]) == null) {
      throw new ArgumentError("must be not null");
    }

    return o;
  }
}
}
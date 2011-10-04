package com.intellij.flex.uiDesigner.flex.states {
import flash.errors.IllegalOperationError;

[Abstract]
public class StaticInstanceReferenceInDeferredParentInstanceBase implements InstanceProvider {
  public var reference:int;
  public var deferredParentInstance:DeferredInstanceFromBytesBase;

  public function reset():void {
    throw new IllegalOperationError("must be called on parent");
  }

  public function getInstance():Object {
    return deferredParentInstance.getReferredChild(reference);
  }

  public function get nullableInstance():Object {
    return deferredParentInstance.getNullableReferredChild(reference);
  }

  public function get bindingExecutor():DeferredInstanceFromBytesBase {
    return deferredParentInstance;
  }
}
}
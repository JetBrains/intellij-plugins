package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;

public class PropertyBindingDeferredTarget extends PropertyBindingTarget {
  private var deferredParentInstance:DeferredInstanceFromBytesBase;
  private var pendingValue:*;

  public function PropertyBindingDeferredTarget(target:DeferredInstanceFromBytesBase, propertyName:String, isStyle:Boolean) {
    deferredParentInstance = target;
    
    super(target, propertyName, isStyle);
  }

  override public function execute(value:Object):void {
    var t:Object = deferredParentInstance.getNullableInstance();
    if (t == null) {
      if (staticValue === undefined) {
        pendingValue = value;
      }
    }
    else if (t != value) {
      applyValue(t, value);
    }
    else if (staticValue !== undefined) {
      applyValue(t, staticValue);
    }
    else if (pendingValue !== undefined) {
      // called from deferredParentInstance as execute binding
      applyValue(t, pendingValue);
      pendingValue = undefined;
    }
  }
}
}

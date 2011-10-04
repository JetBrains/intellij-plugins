package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;

public class PropertyChainBindingDeferredTarget extends PropertyBindingTarget {
  private var deferredParentInstance:DeferredInstanceFromBytesBase;

  private var changeWatcher:Object;

  public function PropertyChainBindingDeferredTarget(target:DeferredInstanceFromBytesBase, propertyName:String, isStyle:Boolean) {
    deferredParentInstance = target;
    
    super(target, propertyName, isStyle);
  }

  override public function execute(value:Object):void {

  }
}
}

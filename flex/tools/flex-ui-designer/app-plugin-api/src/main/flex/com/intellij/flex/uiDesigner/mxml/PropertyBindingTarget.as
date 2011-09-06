package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.BindingTarget;
import com.intellij.flex.uiDesigner.flex.states.StaticInstanceReferenceInDeferredParentInstanceBase;

internal final class PropertyBindingTarget implements BindingTarget {
  private var object:Object;
  protected var propertyName:String;

  private var isStyle:Boolean;
  public var staticValue:Object;

  public function PropertyBindingTarget(object:Object, propertyName:String, isStyle:Boolean) {
    this.object = object;
    this.propertyName = propertyName;
    this.isStyle = isStyle;
  }

  public function execute(value:Object):void {
    var t:Object = object is StaticInstanceReferenceInDeferredParentInstanceBase ? StaticInstanceReferenceInDeferredParentInstanceBase(object).getInstance() : object;
    if (isStyle) {
      t.setStyle(propertyName, staticValue == null ? value : staticValue);
    }
    else {
      t[propertyName] = staticValue == null ? value : staticValue;
    }
  }
}
}
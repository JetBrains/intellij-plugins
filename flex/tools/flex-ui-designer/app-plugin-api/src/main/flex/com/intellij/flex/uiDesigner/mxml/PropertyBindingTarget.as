package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.BindingTarget;

internal final class PropertyBindingTarget implements BindingTarget {
  private var object:Object;
  private var propertyName:String;

  public function PropertyBindingTarget(object:Object, propertyName:String) {
    this.object = object;
    this.propertyName = propertyName;
  }

  public function execute(value:Object):void {
    object[propertyName] = value;
  }
}
}
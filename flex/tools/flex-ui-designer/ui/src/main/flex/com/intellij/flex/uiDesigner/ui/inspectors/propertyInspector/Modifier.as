package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
public class Modifier {
  private var _object:Object;
  public function set object(object:Object):void {
    _object = object;
  }

  public function applyBoolean(description:Object, value:Boolean):void {
    var propertyName:String = description.name;

    _object[propertyName ] = value;
  }
}
}

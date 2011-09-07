package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.UncaughtErrorManager;
import com.intellij.flex.uiDesigner.flex.BindingTarget;
import com.intellij.flex.uiDesigner.flex.states.StaticInstanceReferenceInDeferredParentInstanceBase;

import flash.display.DisplayObject;

internal class PropertyBindingTarget implements BindingTarget {
  private var target:Object;
  protected var propertyName:String;

  private var isStyle:Boolean;
  public var staticValue:*;

  public function PropertyBindingTarget(target:Object, propertyName:String, isStyle:Boolean) {
    this.target = target;
    this.propertyName = propertyName;
    this.isStyle = isStyle;
  }

  public function execute(value:Object):void {
    var t:Object = target is StaticInstanceReferenceInDeferredParentInstanceBase ? StaticInstanceReferenceInDeferredParentInstanceBase(target).getInstance() : target;
    applyValue(t, value);
  }

  protected final function applyValue(t:Object, value:Object):void {
    try {
      if (isStyle) {
        t.setStyle(propertyName, staticValue === undefined ? value : staticValue);
      }
      else {
        t[propertyName] = staticValue === undefined ? value : staticValue;
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.handleUiError(e, value as DisplayObject, "Can't execute binding");
    }
  }
}
}
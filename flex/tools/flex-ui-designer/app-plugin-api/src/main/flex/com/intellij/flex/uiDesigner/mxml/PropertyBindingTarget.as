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

  protected var changeWatcher:Object;
  protected var changeWatcherHost:Object;

  public function PropertyBindingTarget(target:Object, propertyName:String, isStyle:Boolean) {
    this.target = target;
    this.propertyName = propertyName;
    this.isStyle = isStyle;
  }

  public function execute(value:Object):void {
    var t:Object = target is StaticInstanceReferenceInDeferredParentInstanceBase ? StaticInstanceReferenceInDeferredParentInstanceBase(target).getInstance() : target;
    if (changeWatcher != null) {
      if (value == null) {
        resetChangeWatcher();
      }
      else if (!changeWatcher.isWatching()) {
        changeWatcher.reset(value);
        changeWatcher.setHandler(changeWatcherHandler);
        changeWatcherHandler(null);
      }
    }
    else {
      applyValue(t, value);
    }
  }

  public function initChangeWatcher2(value:Object):void {
    changeWatcher = value;
  }

  private function changeWatcherHandler(event:Object):void {
    applyValue(target is StaticInstanceReferenceInDeferredParentInstanceBase
                 ? StaticInstanceReferenceInDeferredParentInstanceBase(target).getInstance() : target, changeWatcher.getValue());
  }

  protected final function resetChangeWatcher():void {
    if (changeWatcher.isWatching()) {
      changeWatcher.reset(null);
      changeWatcher.setHandler(null);
    }
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
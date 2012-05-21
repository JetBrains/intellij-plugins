package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.UncaughtErrorManager;
import com.intellij.flex.uiDesigner.flex.BindingTarget;
import com.intellij.flex.uiDesigner.flex.states.InstanceProvider;
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
    const t:Object = target is StaticInstanceReferenceInDeferredParentInstanceBase
      ? StaticInstanceReferenceInDeferredParentInstanceBase(target).nullableInstance : target;
    if (changeWatcher == null) {
      applyValue(t, value);
    }
    else {
      executeWithChangeWatcher(t, value, true, changeWatcherHandler);
    }
  }

  protected final function executeWithChangeWatcher(t:Object, value:Object, passValue:Boolean, handler:Function):void {
    if (t == null && value != null) {
      return;
    }

    if (value == null) {
      // reset in both cases — or host became null (change state), or target became null
      // we don't set target value as null according to flex compiler (as example — target is static, but host is dynamic)
      resetChangeWatcher();
    }
    else if (!changeWatcher.isWatching()) {
      watchHost(passValue ? value : null, handler);
    }
  }

  public function initChangeWatcher(value:Object, changeWatcherHost:Object):void {
    changeWatcher = value;
    this.changeWatcherHost = changeWatcherHost;
  }

  private function changeWatcherHandler(event:Object):void {
    applyValue(target is StaticInstanceReferenceInDeferredParentInstanceBase
                       ? StaticInstanceReferenceInDeferredParentInstanceBase(target).getInstance() : target, changeWatcher.getValue());
  }

  protected final function watchHost(value:Object, handler:Function):void {
    var newHost:Object;
    if (changeWatcherHost == null) {
      newHost = value;
    }
    else if (changeWatcherHost is InstanceProvider) {
      newHost = InstanceProvider(changeWatcherHost).nullableInstance;
    }
    else {
      newHost = changeWatcherHost;
    }

    if (newHost == null) {
      return;
    }

    changeWatcher.reset(newHost);
    changeWatcher.setHandler(handler);
    handler(null);
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
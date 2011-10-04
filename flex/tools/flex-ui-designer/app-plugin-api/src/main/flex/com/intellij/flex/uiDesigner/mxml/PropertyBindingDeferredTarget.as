package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;

public final class PropertyBindingDeferredTarget extends PropertyBindingTarget {
  private var deferredParentInstance:DeferredInstanceFromBytesBase;
  private var pendingValue:*;

  public function PropertyBindingDeferredTarget(target:DeferredInstanceFromBytesBase, propertyName:String, isStyle:Boolean) {
    deferredParentInstance = target;
    
    super(target, propertyName, isStyle);
  }

  override public function execute(value:Object):void {
    const t:Object = deferredParentInstance.getNullableInstance();

    if (changeWatcher != null) {
      if (t == null && value != null) {
        return;
      }
      
      if (value == null) {
        // reset in both cases — or host became null (change state), or target became null
        // we don't set target value as null according to flex compiler (as example — target is static, but host is dynamic)
        resetChangeWatcher();
      }
      else if (!changeWatcher.isWatching()) {
        var newHost:Object = changeWatcherHost is DeferredInstanceFromBytesBase
          ? DeferredInstanceFromBytesBase(changeWatcherHost).getNullableInstance() : changeWatcherHost;
        if (newHost == null) {
          return;
        }

        changeWatcher.reset(newHost);
        changeWatcher.setHandler(changeWatcherHandler);
        changeWatcherHandler(null);
      }

      return;
    }

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

  public function initChangeWatcher(value:Object, changeWatcherHost:Object):void {
    changeWatcher = value;
    this.changeWatcherHost = changeWatcherHost;
  }

  //noinspection JSUnusedLocalSymbols
  private function changeWatcherHandler(event:Object):void {
    applyValue(deferredParentInstance.getNullableInstance(), changeWatcher.getValue());
  }
}
}

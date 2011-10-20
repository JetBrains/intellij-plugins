package com.intellij.flex.uiDesigner.util {
import flash.errors.IllegalOperationError;
import flash.utils.Dictionary;
import flash.utils.Proxy;
import flash.utils.flash_proxy;

use namespace flash_proxy;

public dynamic class ImmutableFakeObjectProxy extends Proxy {
  protected var mapList:Vector.<Dictionary>;

  public function ImmutableFakeObjectProxy(mapList:Vector.<Dictionary>) {
    this.mapList = mapList;
  }

  override flash_proxy function getProperty(name:*):* {
    var v:*;
    for each (var map:Dictionary in mapList) {
      v = map[name];
      if (v !== undefined) {
        return v;
      }
    }

    return undefined;
  }

  override flash_proxy function hasProperty(name:*):Boolean {
    return getProperty(name.toString()) !== undefined;
  }

  override flash_proxy function deleteProperty(name:*):Boolean {
    throw new IllegalOperationError("forbidden");
  }

  override flash_proxy function setProperty(name:*, value:*):void {
    throw new IllegalOperationError("forbidden");
  }
}
}
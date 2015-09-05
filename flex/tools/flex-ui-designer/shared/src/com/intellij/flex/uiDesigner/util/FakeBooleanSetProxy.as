package com.intellij.flex.uiDesigner.util {
import flash.utils.Dictionary;
import flash.utils.flash_proxy;

use namespace flash_proxy;

public final class FakeBooleanSetProxy extends ImmutableFakeObjectProxy {
  private var userMap:Dictionary;
  
  public function FakeBooleanSetProxy(mapList:Vector.<Dictionary>) {
    super(mapList);
  }

  
  public function addUser(name:String):void {
    if (userMap == null) {
      userMap = new Dictionary();
    }
    
    userMap[name] = true;
  }

  override flash_proxy function getProperty(name:*):* {
    for each (var map:Dictionary in mapList) {
      if (map[name]) {
        return true;
      }
    }

    return userMap != null && userMap[name];
  }

  override flash_proxy function hasProperty(name:*):Boolean {
    return getProperty(name.toString()) === true;
  }
}
}
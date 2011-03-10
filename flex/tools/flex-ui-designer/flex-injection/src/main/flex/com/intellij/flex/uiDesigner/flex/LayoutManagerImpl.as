package com.intellij.flex.uiDesigner.flex {
import mx.managers.LayoutManager;

public class LayoutManagerImpl extends LayoutManager {
  private static var instance:LayoutManagerImpl;

  public function LayoutManagerImpl():void {
    if (instance != null) {
      throw new Error("singleton");
    }
    
    instance = this;
  }

  public static function getInstance():LayoutManagerImpl {
    return instance;
  }

  override public function set usePhasedInstantiation(value:Boolean):void {
  }
}
}
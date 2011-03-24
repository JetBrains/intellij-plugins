package com.intellij.flex.uiDesigner {
import flash.display.DisplayObjectContainer;

public class SwfDataManager {
  private const data:Vector.<Class> = new Vector.<Class>();
  
  public function get(id:int):Class {
    return data[id];
  }
  
  public function register(id:int, swfContent:DisplayObjectContainer):void {
    assert(id == data.length || (id < data.length && data[id] == null));
    // BitmapDataManager is not exlusive owner of id, so, second assert (as in DocumentFactoryManager) is not needed
    // (due to server BinaryFileManager for both client BitmapDataManager and client SwfDataManager) 

    //data[id] = bitmapData;
  }
}
}

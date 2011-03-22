package com.intellij.flex.uiDesigner {
import flash.display.BitmapData;

public class BitmapDataManager {
  private const data:Vector.<BitmapData> = new Vector.<BitmapData>();
  
  public function get(id:int):BitmapData {
    return data[id];
  }
  
  public function register(id:int, bitmapData:BitmapData):void {
    assert(id == data.length || (id < data.length && data[id] == null));
    assert(id == 0 || data[id - 1] != null);
    
    data[id] = bitmapData;
  }
}
}

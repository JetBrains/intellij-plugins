package com.intellij.flex.uiDesigner {
import flash.display.BitmapData;

public class BitmapDataManager {
  private var data:Vector.<BitmapData>;

  public function get(id:int):BitmapData {
    return data[id];
  }

  public function register(id:int, bitmapData:BitmapData):void {
    // BitmapDataManager is not exlusive owner of id, so, asserts (as in DocumentFactoryManager) is not needed
    // (due to server BinaryFileManager for both client BitmapDataManager and client SwfDataManager)
    if (data == null) {
      data = new Vector.<BitmapData>(id + 8);
    }
    else if (id >= data.length) {
      data.length += 8;
    }

    data[id] = bitmapData;
  }
}
}

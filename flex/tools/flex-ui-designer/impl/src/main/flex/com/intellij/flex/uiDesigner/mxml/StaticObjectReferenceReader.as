package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.Dictionary;
import flash.utils.IDataInput;

internal final class StaticObjectReferenceReader {
  private var dataSize:int;

  private var firstObjectOffset:int = 254;
  private var lastObjectOffset:int;
  
  private var map:Dictionary;
  private var table:Vector.<Object>;
  
  public var rootObject:Object;

  public function read(data:IDataInput):void {
    var size:int = AmfUtil.readUInt29(data);
    if (size == 0) {
      return; 
    }
    
    if (table == null) {
      table = new Vector.<Object>(size, true);
    }
    else {
      table.length = size;
      table.fixed = true;
    }
    
    map = new Dictionary();
    // state writer can't write ordered list, so, we can't assume that last item is max item
    for (var i:int = 0; i < size; i++) {
      var offset:int = AmfUtil.readUInt29(data);
      map[offset] = i;
      
      if (offset < firstObjectOffset) {
        firstObjectOffset = offset;
      }
      if (offset > lastObjectOffset) {
        lastObjectOffset = offset;
      }
    }
    
    dataSize = data.bytesAvailable;
  }
  
  public function registerObject(rawOffset:int, object:Object):void {
    if (map == null) {
      return;
    }
    
    var objectOffset:int = dataSize - rawOffset;
    if (objectOffset < firstObjectOffset || objectOffset > lastObjectOffset) {
      return;
    }
    
    var index:* = map[objectOffset];
    if (index === undefined) {
      return;
    }
    
    table[int(index)] = object;
  }

  public function reset():void {
    map = null;
    rootObject = null;
    if (table != null) {
      table.fixed = false;
      table.length = 0;
    }
    
    firstObjectOffset = 254;
    lastObjectOffset = 0;
  }

  public function getObject(i:int):Object {
    if (i == 255) {
      return rootObject;
    }
    
    var object:Object = table[i];
    assert(object != null);
    return object;
  }
}
}
package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.errors.IllegalOperationError;
import flash.utils.IDataInput;

public final class StringRegistry {
  private var table:Vector.<String>;
  
  public function StringRegistry() {
    if (_instance != null) {
      throw new IllegalOperationError();
    }
    
    _instance = this;
  }
  
  public function initStringTable(input:IDataInput):void {
    if (table != null) {
      throw new IllegalOperationError();
    }
    
    table = input.readObject();
  }
  
  public function readStringTable(input:IDataInput):void {
    var size:int = AmfUtil.readUInt29(input);
    if (size == 0) {
      return;
    }
    
    var i:int;
    if (table == null) {
      table = new Vector.<String>(size, true);
    }
    else {
      i = table.length;
      table.fixed = false;
      table.length = i + size;
      table.fixed = true;
    }

    var n:int = table.length;
    for (; i < n; i++) {
      table[i] = input.readUTFBytes(AmfUtil.readUInt29(input));
    }
  }
  
  private static var _instance:StringRegistry;
  public static function get instance():StringRegistry {
    return _instance;
  }
  
  public function read(input:IDataInput):String {
    const ref:int = AmfUtil.readUInt29(input);
    return ref == 0 ? null : table[ref - 1];
  }
  
  public function getTable():Vector.<String> {
    return table;
  }
}
}
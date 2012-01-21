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
      var m:String = "Table already created: ";
      for each (var s:String in table) {
        m += s + " ";
      }
      throw new IllegalOperationError(m);
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

    const n:int = table.length;
    while (i < n) {
      //var s:String = AmfUtil.readUtf(input);
      //if (table.indexOf(s) != -1) {
      //  throw new IllegalOperationError(s + " already registered");
      //}
      //table[i++] = s;
      table[i++] = AmfUtil.readString(input);
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

  public function readNotNull(input:IDataInput):String {
    // ability to debug
    //var i:int = AmfUtil.readUInt29(input);
    //if (i == 0) {
    //  var s:int = 0;
    //  s++;
    //}
    //return table[i - 1];
    return table[AmfUtil.readUInt29(input) - 1];
  }
  
  public function getTable():Vector.<String> {
    return table;
  }
}
}
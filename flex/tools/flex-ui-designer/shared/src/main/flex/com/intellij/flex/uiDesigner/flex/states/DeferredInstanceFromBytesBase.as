package com.intellij.flex.uiDesigner.flex.states {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.errors.IllegalOperationError;
import flash.utils.ByteArray;

public class DeferredInstanceFromBytesBase {
  private var bytes:ByteArray;
  private var objectTable:Vector.<Object>;
  
  protected var instance:Object;
  
  public function DeferredInstanceFromBytesBase(bytes:ByteArray) {
    this.bytes = bytes;
  }

  public function create(context:DeferredInstanceFromBytesContext):Object {
    if (instance == null) {
      instance = context.documentReader.read2(bytes, context);
      objectTable = context.documentReader.getLocalObjectTable();
    }

    return instance;
  }
  
  public function getInstance():Object {
    if (instance == null) {
      throw new IllegalOperationError("must be created before this moment");
    }
    
    return instance;
  }
  
  public function reset():void {
    instance = null;
    objectTable = null;
    bytes.position = 0;
  }
  
  internal function getReferredChild(reference:int):Object {
    var o:Object;
    if ((o = objectTable[reference]) == null) {
      throw new ArgumentError("must be not null");
    }
    
    return o; 
  }
}
}
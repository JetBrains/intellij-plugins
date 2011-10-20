package com.intellij.flex.uiDesigner.flex {
import mx.core.IDeferredInstance;

public class DeferredInstanceFromArray implements IDeferredInstance {
  private var array:Array;
  
  public function DeferredInstanceFromArray(array:Array) {
    this.array = array;
  }

  public function getInstance():Object {
    return array;
  }
}
}
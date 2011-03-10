package com.intellij.flex.uiDesigner.flex {
import mx.core.IDeferredInstance;

public class DeferredInstanceFromArray implements IDeferredInstance {
  public var array:Array;

  public function getInstance():Object {
    return array;
  }
}
}
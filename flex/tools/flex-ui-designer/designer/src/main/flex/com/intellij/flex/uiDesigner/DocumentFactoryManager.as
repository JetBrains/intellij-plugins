package com.intellij.flex.uiDesigner {
public class DocumentFactoryManager {
  private var factories:Vector.<DocumentFactory>;
  
  public function DocumentFactoryManager() {
  }

  public function get(id:int):DocumentFactory {
    return factories[id];
  }
}
}

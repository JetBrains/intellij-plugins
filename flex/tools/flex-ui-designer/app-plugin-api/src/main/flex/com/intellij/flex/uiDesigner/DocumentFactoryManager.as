package com.intellij.flex.uiDesigner {
public class DocumentFactoryManager {
  private const factories:Vector.<DocumentFactory> = new Vector.<DocumentFactory>();

  public function get(id:int):DocumentFactory {
    return factories[id];
  }

  public function register(id:int, documentFactory:DocumentFactory):void {
    assert(id == factories.length || (id < factories.length && factories[id] == null));
    assert(id == 0 || factories[id - 1] != null);
    
    factories[id] = documentFactory;
  }
}
}

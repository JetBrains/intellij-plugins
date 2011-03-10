package com.intellij.flex.uiDesigner {
import flash.utils.Dictionary;

public class ModuleManager {
  private const idMap:Dictionary = new Dictionary();

  public function register(module:Module):void {
    assert(!(module.id in idMap));
    idMap[module.id] = module;
  }

  public function getById(id:int):Module {
    return idMap[id];
  }
  
  public function remove(project:Project):Vector.<Module> {
    var list:Vector.<Module> = new Vector.<Module>();
    var i:int = 0;
    for (var id:Object in idMap) {
      var module:Module = idMap[id];
      if (module.project == project) {
        delete idMap[id];
        list[i++] = module;
      }
    }
    
    list.fixed = true;
    return list;
  }
}
}
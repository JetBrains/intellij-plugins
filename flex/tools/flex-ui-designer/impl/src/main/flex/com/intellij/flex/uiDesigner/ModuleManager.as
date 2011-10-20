package com.intellij.flex.uiDesigner {
public class ModuleManager {
  private const elements:Vector.<Module> = new Vector.<Module>(8);

  public function register(module:Module):void {
    var id:int = module.id;
    if (id >= elements.length) {
      elements.length += 8;
    }
    else {
      assert(elements[id] == null);
    }

    elements[id] = module;
  }

  public function getById(id:int):Module {
    return elements[id];
  }
  
  public function remove(project:Project, procedure:Function):void {
    for (var i:int = 0, n:int = elements.length; i < n; i++) {
      var module:Module = elements[i];
      if (module != null && module.project == project) {
        elements[i] = null;
        procedure(module);
      }
    }
  }
}
}
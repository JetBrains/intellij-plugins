package com.intellij.flex.uiDesigner {
import org.jetbrains.EntityLists;

public class ModuleManager {
  private const elements:Vector.<Module> = new Vector.<Module>();

  public function register(module:Module):void {
    EntityLists.add(elements, module);
  }

  public function getById(id:int):Module {
    return elements[id];
  }
  
  public function unregister(project:Project, procedure:Function):void {
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
package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;

import flash.utils.Dictionary;

import org.jetbrains.EntityLists;
import org.osflash.signals.ISignal;
import org.osflash.signals.Signal;

public class ModuleManager {
  private const elements:Vector.<Module> = new Vector.<Module>();

  private var _moduleUnregistered:ISignal;
  public function get moduleUnregistered():ISignal {
    if (_moduleUnregistered == null) {
      _moduleUnregistered = new Signal();
    }
    return _moduleUnregistered;
  }

  public function register(id:int, project:Project, isApp:Boolean, librarySet:LibrarySet, localStyleHolders:Vector.<LocalStyleHolder>):void {
    EntityLists.add(elements, new Module(id, librarySet, isApp, localStyleHolders));
  }

  public function getById(id:int):Module {
    return elements[id];
  }

  public function unregister(module:Module):void {
    elements[module.id] = null;
    if (_moduleUnregistered != null) {
      _moduleUnregistered.dispatch(module);
    }
  }
  
  public function unregisterBelongToProject(project:Project):void {
    for each (var module:Module in elements) {
      if (module != null && module.project == project) {
        unregister(module);
      }
    }
  }
}
}
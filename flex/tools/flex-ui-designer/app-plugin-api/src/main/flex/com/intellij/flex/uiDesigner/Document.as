package com.intellij.flex.uiDesigner {
import cocoa.View;

import com.intellij.flex.uiDesigner.flex.SystemManagerSB;

public class Document {
  public var file:VirtualFile;
  public var uiComponent:Object;
  public var systemManager:SystemManagerSB;
  public var container:View;
  public var module:Module;
  
  public var tabIndex:int;

  public function Document(file:VirtualFile, module:Module) {
    this.file = file;
    this.module = module;
  }
 
  private var _styleManager:Object;
  public function get styleManager():Object {
    return _styleManager == null ? module.styleManager : _styleManager;
  }
}
}

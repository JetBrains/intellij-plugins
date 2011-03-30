package com.intellij.flex.uiDesigner {
import cocoa.View;

import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;

public class Document {
  private var documentFactory:DocumentFactory;
  
  public var uiComponent:Object;
  public var systemManager:SystemManagerSB;
  public var container:View;
  
  public var tabIndex:int;

  public function Document(documentFactory:DocumentFactory) {
    this.documentFactory = documentFactory;
  }
  
  public function get file():VirtualFile {
    return documentFactory.file;
  }
  
  public function get module():Module {
    return documentFactory.module;
  }
 
  private var _styleManager:StyleManagerEx;
  public function get styleManager():StyleManagerEx {
    return _styleManager == null ? module.styleManager : _styleManager;
  }
}
}

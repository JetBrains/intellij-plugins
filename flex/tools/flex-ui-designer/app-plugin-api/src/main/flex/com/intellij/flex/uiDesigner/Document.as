package com.intellij.flex.uiDesigner {
import cocoa.View;

import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;

public class Document { 
  public var uiComponent:Object;
  public var systemManager:SystemManagerSB;
  public var container:View;
  
  public function Document(documentFactory:DocumentFactory) {
    this._documentFactory = documentFactory;
  }
  
  public function get file():VirtualFile {
    return _documentFactory.file;
  }
  
  public function get module():Module {
    return _documentFactory.module;
  }
 
  private var _styleManager:StyleManagerEx;
  public function get styleManager():StyleManagerEx {
    return _styleManager == null ? module.styleManager : _styleManager;
  }

  private var _documentFactory:DocumentFactory;
  public function get documentFactory():DocumentFactory {
    return _documentFactory;
  }
}
}
package com.intellij.flex.uiDesigner {
import cocoa.View;

import com.intellij.flex.uiDesigner.css.StyleManagerEx;

public class Document {
  public var uiComponent:Object;
  public var displayManager:DocumentDisplayManager;
  public var container:View;
  
  public function Document(documentFactory:DocumentFactory) {
    _documentFactory = documentFactory;
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
  public function set styleManager(value:StyleManagerEx):void {
    _styleManager = value;
  }

  private var _documentFactory:DocumentFactory;
  public function get documentFactory():DocumentFactory {
    return _documentFactory;
  }
}
}
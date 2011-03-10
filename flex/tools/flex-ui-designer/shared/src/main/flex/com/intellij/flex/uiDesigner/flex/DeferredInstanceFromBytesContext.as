package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.VirtualFile;

public final class DeferredInstanceFromBytesContext {
  public function DeferredInstanceFromBytesContext(documentFile:VirtualFile, mxmlReader:DocumentReader, styleManager:Object, moduleContext:ModuleContext) {
    _documentFile = documentFile;
    _documentReader = mxmlReader;
    _moduleContext = moduleContext;
    _styleManager = styleManager;
  }

  private var _moduleContext:ModuleContext;
  public function get moduleContext():ModuleContext {
    return _moduleContext;
  }

  private var _documentFile:VirtualFile;
  public function get documentFile():VirtualFile {
    return _documentFile;
  }

  private var _documentReader:DocumentReader;
  public function get documentReader():DocumentReader {
    return _documentReader;
  }
  
  private var _styleManager:Object;
  public function get styleManager():Object {
    return _styleManager;
  }
}
}

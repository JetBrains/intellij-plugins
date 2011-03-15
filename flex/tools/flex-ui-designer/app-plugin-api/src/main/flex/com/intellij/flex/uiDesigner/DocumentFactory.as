package com.intellij.flex.uiDesigner {
import flash.utils.ByteArray;

public class DocumentFactory implements SerializedDocumentDataProvider {
  public var file:VirtualFile;
  private var _data:ByteArray;
  public var module:Module;
  
  public function DocumentFactory(data:ByteArray, file:VirtualFile, module:Module) {
    _data = data;
    this.file = file;
    this.module = module;
  }

  public function get data():ByteArray {
    return _data;
  }
}
}

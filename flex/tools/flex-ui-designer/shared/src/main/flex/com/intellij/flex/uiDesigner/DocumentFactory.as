package com.intellij.flex.uiDesigner {
import flash.utils.ByteArray;

public class DocumentFactory {
  public var file:VirtualFile;
  public var bytes:ByteArray;
  
  public function DocumentFactory(bytes:ByteArray) {
    this.bytes = bytes;
  }
}
}

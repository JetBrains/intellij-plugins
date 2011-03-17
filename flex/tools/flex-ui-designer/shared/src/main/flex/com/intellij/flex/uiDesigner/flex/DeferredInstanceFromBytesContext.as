package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReaderContext;

public final class DeferredInstanceFromBytesContext {
  public function DeferredInstanceFromBytesContext(readerContext:DocumentReaderContext, reader:DocumentReader, styleManager:Object) {
    _readerContext = readerContext;
    _reader = reader;
    _styleManager = styleManager;
  }

  private var _readerContext:DocumentReaderContext;
  public function get readerContext():DocumentReaderContext {
    return _readerContext;
  }

  private var _reader:DocumentReader;
  public function get reader():DocumentReader {
    return _reader;
  }
  
  private var _styleManager:Object;
  public function get styleManager():Object {
    return _styleManager;
  }
}
}

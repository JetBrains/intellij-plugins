package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.DocumentReaderContext;

public final class DeferredInstanceFromBytesContext {
  public function DeferredInstanceFromBytesContext(readerContext:DocumentReaderContext, reader:DocumentReader) {
    _readerContext = readerContext;
    _reader = reader;
  }

  private var _readerContext:DocumentReaderContext;
  public function get readerContext():DocumentReaderContext {
    return _readerContext;
  }

  private var _reader:DocumentReader;
  public function get reader():DocumentReader {
    return _reader;
  }
}
}

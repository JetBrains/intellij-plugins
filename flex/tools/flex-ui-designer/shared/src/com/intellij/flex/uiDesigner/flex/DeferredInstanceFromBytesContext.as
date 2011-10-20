package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

public final class DeferredInstanceFromBytesContext {
  public function DeferredInstanceFromBytesContext(readerContext:DocumentReaderContext, reader:DocumentReader, styleManager:StyleManagerEx) {
    _readerContext = readerContext;
    _reader = reader;
    this._styleManager = styleManager;
  }

  private var _readerContext:DocumentReaderContext;
  public function get readerContext():DocumentReaderContext {
    return _readerContext;
  }

  private var _reader:DocumentReader;
  public function get reader():DocumentReader {
    return _reader;
  }

  private var _styleManager:StyleManagerEx;
  public function get styleManager():StyleManagerEx {
    return _styleManager;
  }
}
}
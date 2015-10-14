package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

import flash.errors.IllegalOperationError;

[Abstract]
public class DeferredInstanceFromBytesContext {
  public function DeferredInstanceFromBytesContext(readerContext:DocumentReaderContext, styleManager:StyleManagerEx) {
    _readerContext = readerContext;
    _styleManager = styleManager;
  }

  private var _readerContext:DocumentReaderContext;
  public function get readerContext():DocumentReaderContext {
    return _readerContext;
  }

  public function createReader():DocumentReader {
    throw new IllegalOperationError("abstract");
  }

  private var _styleManager:StyleManagerEx;
  public function get styleManager():StyleManagerEx {
    return _styleManager;
  }
}
}
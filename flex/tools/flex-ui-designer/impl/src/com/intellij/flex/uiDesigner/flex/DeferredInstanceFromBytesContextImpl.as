package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.mxml.FlexMxmlReader;

public final class DeferredInstanceFromBytesContextImpl extends DeferredInstanceFromBytesContext {
  public function DeferredInstanceFromBytesContextImpl(readerContext:DocumentReaderContext, styleManager:StyleManagerEx) {
    super(readerContext, styleManager);
  }

  override public function createReader():DocumentReader {
    return new FlexMxmlReader();
  }
}
}

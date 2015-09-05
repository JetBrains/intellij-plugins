package com.intellij.flex.uiDesigner;

public interface DocumentRenderedListener {
  void documentRendered(DocumentFactoryManager.DocumentInfo info);
  void errorOccurred();
}
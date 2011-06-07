package com.intellij.flex.uiDesigner;

public interface FlexUIDesignerApplicationListener {
  void initialDocumentOpened();
  // doesn't dispatch on idea exit (i.e. Idea Application closed)
  void applicationClosed();
}

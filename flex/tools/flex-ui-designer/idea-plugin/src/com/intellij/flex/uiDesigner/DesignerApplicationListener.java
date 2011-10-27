package com.intellij.flex.uiDesigner;

public interface DesignerApplicationListener {
  void initialDocumentOpened();
  // doesn't dispatch on idea exit (i.e. Idea Application closed)
  void applicationClosed();
}

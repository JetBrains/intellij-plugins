package com.intellij.flex.uiDesigner.ui {
import cocoa.pane.PaneItem;

import com.intellij.flex.uiDesigner.Document;

internal final class DocumentPaneItem extends PaneItem {
  private var _document:Document;
  public function get document():Document {
    return _document;
  }

  public function DocumentPaneItem(document:Document) {
    _document = document;
    view = document.container;

    super(null, null);
  }
}
}

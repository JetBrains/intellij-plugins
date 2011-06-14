package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.tableView.TextLineLinkedListEntry;

import flash.display.DisplayObject;
import flash.text.engine.TextLine;

public class TextLineAndDisplayObjectLinkedListEntry extends TextLineLinkedListEntry {
  public var displayObject:DisplayObject;
  private var entryFactory:TextLineAndDisplayObjectLinkedListEntryFactory;

  function TextLineAndDisplayObjectLinkedListEntry(line:TextLine, displayObject:DisplayObject, entryFactory:TextLineAndDisplayObjectLinkedListEntryFactory) {
    super(line);
    this.displayObject = displayObject;
    this.entryFactory = entryFactory;
  }

  override public function addToPool():void {
    entryFactory.addToPool(this);
  }
}
}

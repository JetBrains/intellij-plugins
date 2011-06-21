package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import cocoa.CheckBox;
import cocoa.plaf.basic.ButtonSkinInteraction;
import cocoa.tableView.TextLineLinkedListEntry;

public class CheckBoxLinkedListEntry extends TextLineLinkedListEntry {
  internal static const pool1:Vector.<CheckBoxLinkedListEntry> = new Vector.<CheckBoxLinkedListEntry>(32, true);
  internal static var poolSize1:int;

  internal static var oldPoolSize1:int;

  public var checkbox:CheckBox;

  public function get interaction():ButtonSkinInteraction {
    return ButtonSkinInteraction(checkbox.skin);
  }

  function CheckBoxLinkedListEntry(selected:Boolean) {
    checkbox = new CheckBox();
    checkbox.selected = selected;

    super(null);
  }

  public static function create(selected:Boolean):CheckBoxLinkedListEntry {
    if (poolSize1 == 0) {
      return new CheckBoxLinkedListEntry(selected);
    }
    else {
      var entry:CheckBoxLinkedListEntry = pool1[--poolSize1];
      entry.checkbox.selected = selected;
      return entry;
    }
  }

  override public function addToPool():void {
    if (poolSize1 == pool1.length) {
      pool1.fixed = false;
      pool1.length = poolSize1 << 1;
      pool1.fixed = true;
    }
    pool1[poolSize1++] = this;
  }
}
}

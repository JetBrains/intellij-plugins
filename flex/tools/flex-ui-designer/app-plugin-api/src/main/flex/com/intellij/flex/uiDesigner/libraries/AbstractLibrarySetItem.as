package com.intellij.flex.uiDesigner.libraries {
[Abstract]
internal class AbstractLibrarySetItem {
  public function AbstractLibrarySetItem(parents:Vector.<LibrarySetItem>) {
    _parents = parents;
  }

  private var _parents:Vector.<LibrarySetItem>;
  public function get parents():Vector.<LibrarySetItem> {
    return _parents;
  }

  private var _loadState:int;
  public function get loadState():int {
    return _loadState;
  }
  public function set loadState(value:int):void {
    _loadState = value;
  }
}
}

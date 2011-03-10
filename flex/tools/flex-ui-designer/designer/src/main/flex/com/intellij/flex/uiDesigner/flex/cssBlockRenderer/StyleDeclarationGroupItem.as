package com.intellij.flex.uiDesigner.flex.cssBlockRenderer {
import flash.display.DisplayObjectContainer;

public class StyleDeclarationGroupItem {
  public function StyleDeclarationGroupItem(owner:DisplayObjectContainer) {
    _owner = owner;
  }

  // null for global
  private var _owner:DisplayObjectContainer;
  public function get owner():DisplayObjectContainer {
    return _owner;
  }
}
}

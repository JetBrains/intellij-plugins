package com.intellij.flex.uiDesigner.ui.inspectors {
import cocoa.FlexDataGroup;
import cocoa.Panel;

public class AbstractInspector extends Panel {
  public var list:FlexDataGroup;

  override protected function skinAttached():void {
    super.skinAttached();

    list.laf = laf;
  }
}
}

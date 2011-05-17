package com.intellij.flex.uiDesigner.ui.inspectors {
import cocoa.FlexDataGroup;
import cocoa.Panel;

import org.flyti.plexus.Injectable;

public class AbstractInspector extends Panel implements Injectable {
  public var list:FlexDataGroup;

  override protected function skinAttachedHandler():void {
    super.skinAttachedHandler();

    list.laf = laf;
  }
}
}

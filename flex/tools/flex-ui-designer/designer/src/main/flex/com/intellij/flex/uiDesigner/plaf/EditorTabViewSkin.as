package com.intellij.flex.uiDesigner.plaf {
import cocoa.Insets;
import cocoa.plaf.basic.AbstractTabViewSkin;

import flash.display.Shape;

import mx.core.IInvalidating;

internal class EditorTabViewSkin extends AbstractTabViewSkin {
  private static const CONTENT_INSETS:Insets = new Insets(1, 25 /* tab title bar */ + 25 /* editor toolbar (always exists in our case — states bar for example) */, 1, 1);
  
  private var _borderShape:Shape;
  public function get borderShape():Shape {
    return _borderShape;
  }
  
  override public function get contentInsets():Insets {
    return CONTENT_INSETS;
  }
  
  override protected function createChildren():void {
    super.createChildren();
    
    assert(_borderShape == null);
    _borderShape = new Shape();
    addDisplayObject(_borderShape);
  }
  
  override protected function measure():void {
    // skip
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    super.updateDisplayList(w, h);

    const selectedIndex:int = segmentedControl.selectedIndex;
    if (selectedIndex != -1) {
      IInvalidating(segmentedControl.getElementAt(selectedIndex)).invalidateDisplayList();
    }
  }
}
}

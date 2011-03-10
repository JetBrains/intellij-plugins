package com.intellij.flex.uiDesigner.flex.tools {
import flash.display.DisplayObject;
import flash.display.Sprite;

public class SelectionKnobsTool implements Tool, ElementLayoutChangeListener {
  [Embed(source="/viewSelectionKnob.png")]
  private static var viewSelectionKnobClass:Class;

  private var selectionKnobs:Sprite;

  public function attach(element:Object, toolContainer:ElementToolContainer):void {
    if (selectionKnobs == null) {
      selectionKnobs = new Sprite();
      toolContainer.addDisplayObject(selectionKnobs);
      for (var i:int = 0; i < 8; i++) {
        selectionKnobs.addChild(new viewSelectionKnobClass());
      }
    }
    else {
      selectionKnobs.visible = true;
    }
  }

  public function detach():void {
    selectionKnobs.visible = false;
  }

  public function sizeHandler(w:Number, h:Number):void {
    for (var i:int = 0; i < 8; i++) {
      var knob:DisplayObject = selectionKnobs.getChildAt(i);

      if (i < 3) {
        knob.y = -3;
      }
      else if (i > 4) {
        knob.y = h - 3;
      }
      else {
        knob.y = (h / 2) - 3;
      }

      if (i == 0 || i == 3 || i == 5) {
        knob.x = -3;
      }
      else if (i == 1 || i == 6) {
        knob.x = (w / 2) - 3;
      }
      else {
        knob.x = w - 3;
      }
    }
  }
}
}

package com.intellij.flex.uiDesigner.test {
import flash.display.DisplayObject;
import flash.display.Sprite;

[DefaultProperty("mxmlContent")]
public class FlashContainer extends Sprite {
  private var _mxmlContent:Vector.<DisplayObject>;
  public function set mxmlContent(value:Vector.<DisplayObject>):void {
    if (value == _mxmlContent) {
      return;
    }

    if (_mxmlContent != null) {
      for each (var child:DisplayObject in _mxmlContent) {
        removeChild(child);
      }
    }

    _mxmlContent = value;
    if (_mxmlContent != null) {
      createChildren();
    }
  }

  protected function createChildren():void {
    for each (var child:DisplayObject in _mxmlContent) {
      addChild(child);
    }
  }
}
}

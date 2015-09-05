package com.intellij.flex.uiDesigner.flex {
import mx.events.FlexEvent;

import spark.components.IItemRenderer;

public final class UnknownItemRenderer extends UnknownComponent implements IItemRenderer {
  private var _data:Object;

  [Bindable("dataChange")]
  public function get data():Object {
    return _data;
  }

  public function set data(value:Object):void {
    _data = value;
    if (hasEventListener(FlexEvent.DATA_CHANGE)) {
      dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
    }
  }

  private var _itemIndex:int;

  public function get itemIndex():int {
    return _itemIndex;
  }

  public function set itemIndex(value:int):void {
    if (value == _itemIndex) {
      return;
    }

    _itemIndex = value;
    invalidateDisplayList();
  }

  private var _dragging:Boolean;
  public function get dragging():Boolean {
    return _dragging;
  }

  public function set dragging(value:Boolean):void {
    _dragging = value;
  }

  private var _label:String = "";
  public function get label():String {
    return _label;
  }

  public function set label(value:String):void {
    _label = value;
  }

  private var _selected:Boolean;
  public function get selected():Boolean {
    return _selected;
  }

  public function set selected(value:Boolean):void {
    _selected = value;
  }

  private var _showsCaret:Boolean;
  public function get showsCaret():Boolean {
    return _showsCaret;
  }

  public function set showsCaret(value:Boolean):void {
    if (value == _showsCaret) {
      return;
    }

    _showsCaret = value;
  }

  override protected function measure():void {
    measuredWidth = 96;
    measuredHeight = 22;
  }

  override protected function createStatusText():String {
    // todo unknown itemRenderer class name (if specified) or itemRendererFunction (if specified)
    return "Unknown renderer #" + itemIndex;
  }
}
}

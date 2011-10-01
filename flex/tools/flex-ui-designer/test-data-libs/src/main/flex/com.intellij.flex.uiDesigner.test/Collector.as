package com.intellij.flex.uiDesigner.test {
import mx.core.UIComponent;
import mx.utils.ObjectUtil;

import spark.components.Label;

public class Collector extends UIComponent {
  private var label:Label;

  public function Collector() {
    width = 200;
    height = 100;
  }

  override protected function createChildren():void {
    super.createChildren();

    label = new Label();
    label.width = 200;
    label.height = 100;
    addChild(label);
  }

  private var _data:Object;
  public function get data():Object {
    return _data;
  }

  public function set data(value:Object):void {
    _data = value;
    label.text = ObjectUtil.toString(value);
  }
}
}

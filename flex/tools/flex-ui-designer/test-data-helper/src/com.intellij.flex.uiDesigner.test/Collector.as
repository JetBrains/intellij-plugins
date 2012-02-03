package com.intellij.flex.uiDesigner.test {
import mx.utils.ObjectUtil;

import spark.components.RichEditableText;
import spark.components.Scroller;

[DefaultProperty("data")]
public class Collector extends Scroller {
  private var textView:RichEditableText;

  public function Collector() {
    width = 200;
    height = 200;
  }

  override protected function createChildren():void {
    super.createChildren();

    textView = new RichEditableText();
    textView.editable = false;
    textView.selectable = false;

    textView.width = 200;
    textView.height = 200;
    applyData();

    viewport = textView;
  }

  private var _data:Object;
  public function get data():Object {
    return _data;
  }

  public function set data(value:Object):void {
    _data = value;
    if (textView != null) {
      applyData();
    }
  }

  private function applyData():void {
    textView.text = ObjectUtil.toString(_data);
  }
}
}

package com.intellij.flex.uiDesigner.css {
[Abstract]
internal class AbstractCssCondition {
  public function AbstractCssCondition(value:String) {
    _value = value;
  }
  
  protected var _value:String;
  public function get value():String {
    return _value;
  }
}
}

package com.intellij.flex.uiDesigner.css {
public class CssIdCondition extends AbstractCssCondition implements CssCondition {
  public function CssIdCondition(value:String) {
    super(value);
  }

  public function matches(object:Object):Boolean {
    return object.id == _value;
  }
  
  

  public function get specificity():int {
    return 100;
  }
  
  public function appendString(text:String):String {
    text += "#" + value;
    return text;
  }
}
}

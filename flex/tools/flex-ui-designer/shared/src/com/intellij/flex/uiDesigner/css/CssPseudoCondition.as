package com.intellij.flex.uiDesigner.css {
public class CssPseudoCondition extends AbstractCssCondition implements CssCondition {
  public function CssPseudoCondition(value:String) {
    super(value);
  }

  public function matches(object:Object):Boolean {
    return object.matchesCSSState(_value);
  }
  
  public function get specificity():int {
    return 10;
  }

  public function appendString(text:String):String {
    text += ":" + value;
    return text;
  }
}
}

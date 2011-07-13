package com.intellij.flex.uiDesigner.css {
public class CssClassCondition extends AbstractCssCondition implements CssCondition {
  private var regExp:RegExp;

  public function CssClassCondition(value:String) {
    super(value);
  }

  public function matches(object:Object):Boolean {
    if (regExp == null) {
      regExp = new RegExp("\\s?" + value + "\\s?");
    }

    var styleName:String = object.styleName as String;
    return styleName != null ? styleName.search(regExp) != -1 : false;
  }

  public function get specificity():int {
    return 10;
  }
  
  public function appendString(text:String):String {
    text += "." + value;
    return text;
  }
}
}

package com.intellij.flex.uiDesigner.css {
import mx.core.mx_internal;

use namespace mx_internal;

public class InlineCssStyleDeclaration extends AbstractCssStyleDeclaration {
  private var _ruleset:CssRuleset;

  public function InlineCssStyleDeclaration(ruleset:CssRuleset, styleManager:StyleManagerEx) {
    _ruleset = ruleset;

    super(styleManager);
  }

  public function get ruleset():CssRuleset {
    return _ruleset;
  }

  override public function getStyle(styleProp:String):* {
    var v:CssDeclaration = _ruleset.declarationMap[styleProp];
    if (v != null && v.value !== undefined) {
      return styleManager.styleValueResolver.resolve(v);
    }

    return undefined;
  }

  override public function setStyle(styleProp:String, newValue:*):void {
    _ruleset.put(styleProp, newValue);
    // todo cm.notifyStyleChangeInChildren(styleProp, true); is need?
  }

  override mx_internal function setLocalStyle(styleProp:String, newValue:*):void {
    _ruleset.put(styleProp, newValue);
  }
}
}

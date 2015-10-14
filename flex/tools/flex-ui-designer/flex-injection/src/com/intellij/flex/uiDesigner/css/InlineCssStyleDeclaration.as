package com.intellij.flex.uiDesigner.css {
import mx.core.mx_internal;

use namespace mx_internal;

public class InlineCssStyleDeclaration extends AbstractCssStyleDeclaration {
  protected var _ruleset:InlineCssRuleset;

  public function InlineCssStyleDeclaration(ruleset:InlineCssRuleset, styleValueResolver:StyleValueResolver) {
    _ruleset = ruleset;

    super(styleValueResolver);
  }

  public function get ruleset():InlineCssRuleset {
    return _ruleset;
  }

  override public function getStyle(styleProp:String):* {
    var v:CssDeclaration = ruleset.declarationMap[styleProp];
    if (v != null && v.value !== undefined) {
      return styleValueResolver.resolve(v);
    }

    return undefined;
  }

  override public function setStyle(styleProp:String, newValue:*):void {
    ruleset.put(styleProp, newValue);
    // todo cm.notifyStyleChangeInChildren(styleProp, true); is need?
  }

  override mx_internal function setLocalStyle(styleProp:String, newValue:*):void {
    ruleset.put(styleProp, newValue);
  }

  override public function set defaultFactory(value:Function):void {
    // see mx.charts.AxisRenderer.initStyles HaloDefaults.createSelector
    // styleManager.getStyleDeclaration(selectorName) returns our MergedCssStyleDeclaration and this method will be called
    InlineCssRuleset.fillFromFactory(value, ruleset);
  }
}
}

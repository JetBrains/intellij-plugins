package com.intellij.flex.uiDesigner.css {
import flash.errors.IllegalOperationError;

import mx.core.FlexVersion;

import mx.core.mx_internal;
import mx.styles.IAdvancedStyleClient;

use namespace mx_internal;

public class MergedCssStyleDeclaration extends AbstractCssStyleDeclaration implements MergedCssStyleDeclarationEx {
  private var _rulesets:Vector.<CssRuleset>;
  private var mySelector:CssSelector;
 
  public function MergedCssStyleDeclaration(selector:CssSelector, ruleset:CssRuleset, styleValueResolver:StyleValueResolver) {
    super(styleValueResolver);

    if (ruleset != null) {
      _rulesets = new Vector.<CssRuleset>(1);
      _rulesets[0] = ruleset;
    }

    mySelector = selector;
  }
  
  public static function mergeDeclarations(selector:String, style:MergedCssStyleDeclaration, parentStyle:MergedCssStyleDeclaration, styleValueResolver:StyleValueResolver):MergedCssStyleDeclaration {
    var merged:MergedCssStyleDeclaration = new MergedCssStyleDeclaration(new CssSelector(selector, null, null, null, null), null, styleValueResolver);
    merged._rulesets = style._rulesets.concat(parentStyle._rulesets);
    merged._rulesets.fixed = true;
    return merged;
  }

  public function addRuleset(value:CssRuleset):void {
    _rulesets.push(value);
  }

  public function get rulesets():Vector.<CssRuleset> {
    return _rulesets;
  }

  override public function getStyle(styleProp:String):* {
    var v:*;
    for each (var ruleset:CssRuleset in _rulesets) {
      v = ruleset.declarationMap[styleProp];
      if (v !== undefined) {
        return styleValueResolver.resolve(v);
      }
    }
    
    return undefined;
  }

  override mx_internal function get selectorString():String {
    throw new IllegalOperationError();
  }

  override public function get subject():String {
    throw new IllegalOperationError();
  }

  override public function setStyle(styleProp:String, newValue:*):void {
    // see mx.controls.ButtonBar line 528 in flex sdk 4.1
    if (!(FlexVersion.compatibilityVersion == 0x04000000 && styleProp == "borderSkin" && mySelector.subject == "mx.controls.ButtonBar" && mySelector.conditions == null && mySelector.ancestor == null)) {
      throw new IllegalOperationError();
    }
  }

  override mx_internal function getPseudoCondition():String {
    throw new IllegalOperationError();
  }
  
  override mx_internal function isAdvanced():Boolean {
    throw new IllegalOperationError();
  }
  
  override public function matchesStyleClient(object:IAdvancedStyleClient):Boolean {
    return mySelector.matches(object);
  }

  override public function get specificity():int {
    return mySelector.specificity;
  }

  [Inspectable(environment="none")]
  override public function get defaultFactory():Function {
    throw new IllegalOperationError();
  }

  override public function set defaultFactory(value:Function):void {
    // see mx.charts.AxisRenderer.initStyles HaloDefaults.createSelector
    // styleManager.getStyleDeclaration(selectorName) returns our MergedCssStyleDeclaration and this method will be called
    addRuleset(InlineCssRuleset.createExternalInlineWithFactory(value));
  }
}
}
package com.intellij.flex.uiDesigner.css {
import flash.errors.IllegalOperationError;

import mx.core.mx_internal;
import mx.styles.IAdvancedStyleClient;

use namespace mx_internal;

public class MergedCssStyleDeclaration extends AbstractCssStyleDeclaration implements MergedCssStyleDeclarationEx {
  private var _rulesets:Vector.<CssRuleset>;
  private var mySelector:CssSelector;
 
  public function MergedCssStyleDeclaration(selector:CssSelector, ruleset:CssRuleset, styleManager:StyleManagerEx) {
    super(styleManager);

    if (ruleset != null) {
      _rulesets = new Vector.<CssRuleset>(1);
      _rulesets[0] = ruleset;
    }

    mySelector = selector;
  }
  
  public static function mergeDeclarations(selector:String, style:MergedCssStyleDeclaration, parentStyle:MergedCssStyleDeclaration, styleManager:StyleManagerEx):MergedCssStyleDeclaration {
    var merged:MergedCssStyleDeclaration = new MergedCssStyleDeclaration(new CssSelector(selector, null, null, null, null), null, styleManager);
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
        return styleManager.styleValueResolver.resolve(v);
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
    throw new IllegalOperationError();
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
}
}
package com.intellij.flex.uiDesigner.css {
import flash.errors.IllegalOperationError;

import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IAdvancedStyleClient;

use namespace mx_internal;

public final class RuntimeCssStyleDeclaration extends InlineCssStyleDeclaration {
  private var source:CSSStyleDeclaration;
  private var mySelector:CssSelector;
  
  public function RuntimeCssStyleDeclaration(selector:CssSelector, source:CSSStyleDeclaration, styleValueResolver:StyleValueResolver) {
    super(null, styleValueResolver);
    this.source = source;
    mySelector = selector;
  }

  override public function get ruleset():InlineCssRuleset {
    if (_ruleset == null) {
      _ruleset = InlineCssRuleset.createExternalInlineWithFactory(source.defaultFactory, true);
      source = null;
    }

    return _ruleset;
  }

  override mx_internal function get selectorString():String {
     throw new IllegalOperationError();
   }

   override public function get subject():String {
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
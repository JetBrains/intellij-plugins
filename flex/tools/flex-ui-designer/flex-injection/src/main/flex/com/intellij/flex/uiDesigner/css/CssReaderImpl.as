package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.VirtualFile;

import flash.utils.Dictionary;

public class CssReaderImpl implements CssReader {
  private const declarationsBySubject:Dictionary = new Dictionary();
  private var rootStyleDeclaration:MergedCssStyleDeclaration;
  
  private var _styleManager:AbstractStyleManager;
  public function set styleManager(value:StyleManagerEx):void {
    if (_styleManager != null) {
      throw new ArgumentError("_styleManager must be set only once");
    }
    _styleManager = AbstractStyleManager(value);
  }

  public function read(rulesets:Vector.<CssRuleset>, file:VirtualFile):void {
    for each (var ruleset:CssRuleset in rulesets) {
      ruleset.file = file;
      for each (var selector:CssSelector in ruleset.selectors) {
        readSimpleSelectors(ruleset, selector);
      }
    }
  }

  public function finalizeRead():void {
    if (rootStyleDeclaration != null) {
      StyleManagerEx(_styleManager).setRootDeclaration(rootStyleDeclaration);
    }
  }

  private function readSimpleSelectors(ruleset:CssRuleset, selector:CssSelector):void {
    var declaration:MergedCssStyleDeclaration;
    const isDescendantSelector:Boolean = selector.ancestor != null;
    if (!isDescendantSelector && selector.conditions == null) {
      declaration = declarationsBySubject[selector.subject];
      if (declaration != null) {
        declaration.addRuleset(ruleset);
        return;
      }
    }
    
    declaration = new MergedCssStyleDeclaration(selector, ruleset, StyleManagerEx(_styleManager));
    if (!isDescendantSelector) {
      if (selector.conditions == null) {
        if (rootStyleDeclaration == null && selector.subject == "global") {
          rootStyleDeclaration = declaration;
        }
        _styleManager.registerStyleDeclarationWithOnlyTypeSelector(selector.subject, declaration);
        declarationsBySubject[selector.subject] = declaration;
        return;
      }
      else if (selector.conditions.length == 1 && selector.conditions[0] is CssClassCondition) {
        _styleManager.registerStyleDeclarationWithOnlyClassCondition(selector, declaration);
        return;
      }
    }
    
    _styleManager.registerStyleDeclarationWithAdvancedSelector(selector, selector.subject, declaration);
  }
}
}

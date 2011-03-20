package com.intellij.flex.uiDesigner.flex.cssBlockRenderer {
import com.intellij.flex.uiDesigner.css.CssRuleset;

import flash.text.engine.TextBlock;

import org.tinytlf.conversion.ITextBlockFactory;

public class CssRulesetTextBlockFactory extends TextBlockFactory implements ITextBlockFactory {
  private var _declarationPrinter:CssRulesetPrinter;
  public function set declarationPrinter(value:CssRulesetPrinter):void {
    _declarationPrinter = value;
  }

  private var ruleset:CssRuleset;
  public function get data():Object {
    return ruleset;
  }

  public function set data(value:Object):void {
    if (value == ruleset) {
      return; 
    }
    
    ruleset = CssRuleset(value);
    markDataChanged(value);
  }
  
  public function get numBlocks():int {
    if (ruleset.inline) {
      return ruleset.file == null ? ruleset.declarations.length : ruleset.declarations.length + 1;
    }
    else {
      return ruleset.declarations.length + 2 /* selector + close brace */ + 1;
    }
  }

  override protected function createBlock(index:int):TextBlock {
    return _declarationPrinter.createTextBlock(ruleset, index);
  }
}
}

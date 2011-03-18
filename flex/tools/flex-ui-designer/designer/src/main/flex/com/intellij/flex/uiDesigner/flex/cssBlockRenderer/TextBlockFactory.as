package com.intellij.flex.uiDesigner.flex.cssBlockRenderer {
import com.intellij.flex.uiDesigner.css.CssRuleset;

import flash.errors.IllegalOperationError;
import flash.text.engine.TextBlock;

import org.tinytlf.ITextEngine;
import org.tinytlf.conversion.IContentElementFactory;
import org.tinytlf.conversion.ITextBlockFactory;
import org.tinytlf.conversion.ITextBlockGenerator;

public class TextBlockFactory implements ITextBlockFactory {
  private var dataChanged:Boolean;
  
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
    if (f) {
      var f:String;
      f = "";
    }
    
    ruleset = CssRuleset(value);
    if (_engine != null && value != null) {
      dataChanged = true;
      _engine.invalidate();
    }
  }
  
  public function get numBlocks():int {
    return ruleset.inline ? (ruleset.declarations.length + 1) : (ruleset.declarations.length + 2 /* selector + close brace */ + 1 /* file source */);
  }

  private var _engine:ITextEngine;
  public function get engine():ITextEngine {
    return _engine;
  }

  public function set engine(value:ITextEngine):void {
    _engine = value;
  }

  public function preRender():void {
    if (dataChanged) {
      dataChanged = false;
      _engine.analytics.clear();
    }
  }

  public function getTextBlock(index:int):TextBlock {
    var block:TextBlock = _engine.analytics.getBlockAt(index);
    if (block != null) {
      return block;
    }
    
    return _declarationPrinter.createTextBlock(ruleset, index);
  }

  public function hasElementFactory(element:*):Boolean {
    throw new IllegalOperationError();
  }

  public function getElementFactory(element:*):IContentElementFactory {
    throw new IllegalOperationError();
  }

  public function mapElementFactory(element:*, factoryClassOrFactory:Object):void {
    throw new IllegalOperationError();
  }

  public function unMapElementFactory(element:*):Boolean {
    throw new IllegalOperationError();
  }

  public function get textBlockGenerator():ITextBlockGenerator {
    throw new IllegalOperationError();
  }

  public function set textBlockGenerator(value:ITextBlockGenerator):void {
    throw new IllegalOperationError();
  }
}
}

package com.intellij.flex.uiDesigner.flex.cssBlockRenderer {
import cocoa.AbstractView;
import cocoa.plaf.LookAndFeel;
import cocoa.plaf.LookAndFeelProvider;

import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.flex.StylePaneContext;

import mx.core.IDataRenderer;

import org.tinytlf.layout.TinytlfSprite;

import spark.components.DataGroup;

public class CssRulesetItemRenderer extends AbstractView implements IDataRenderer, LookAndFeelProvider {
  private static const TEXT_TOP:Number = 5;
  
  private var textEngine:FlexTextEngine;
  private var displayContainer:TinytlfSprite;
  private var textContainer:TextContainer;

  private var _laf:LookAndFeel;
  public function get laf():LookAndFeel {
    return _laf;
  }
  public function set laf(value:LookAndFeel):void {
    if (_laf == value) {
      return;
    }

    _laf = value;
  }

  private var _data:CssRuleset;
  public function get data():Object {
    return _data;
  }

  public function set data(value:Object):void {
    if (value == null) {
      textEngine.blockFactory.data = null;
      return;
    }

    _data = CssRuleset(value);
    
    var rulesetPrinter:CssRulesetPrinter = StylePaneContext(document).rulesetPrinter;
    if (textEngine == null) {
      init(rulesetPrinter.availableWidth - 10, scrollHandler, nestLevel + 1);
      TextBlockFactory(textEngine.blockFactory).declarationPrinter = rulesetPrinter;

      displayContainer.y = TEXT_TOP;
      displayContainer.x = 5;
      
      addDisplayObject(displayContainer);
    }
    else {
      textEngine.handValidation = true;
      textContainer.explicitWidth = rulesetPrinter.availableWidth - 10;
    }
    
    textEngine.blockFactory.data = _data;
    invalidateSize();
    invalidateDisplayList();
  }
  
  private function init(width:Number, scrollHandler:Function, nestLevel:int):void {
    textEngine = new FlexTextEngine(scrollHandler);
    textEngine.handValidation = true;
    textEngine.nestLevel = nestLevel;
    displayContainer = new TinytlfSprite(textEngine);
    textContainer = new TextContainer(displayContainer, width);
    textEngine.layout.addContainer(textContainer);
    textContainer.scrollable = false;
    
    textEngine.blockFactory = new TextBlockFactory();
    textEngine.decor = new TextDecoration(textContainer.foregroundShape, StylePaneContext(document));

    StylePaneContext(document).rulesetPrinter.interactor.configureTextEngine(textEngine);
  }

  override protected function measure():void {
    textEngine.handValidation = false;
    measuredWidth = textContainer.explicitWidth;
    measuredHeight = textTotalHeight;
  }

  private function get textTotalHeight():Number {
    return Math.ceil(textContainer.measuredHeight) + TEXT_TOP;
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    if (w == 0) {
      return;
    }

    const newW:Number = w - 10;
    if (textContainer.explicitWidth > newW) {
      textContainer.explicitWidth = newW;
      textEngine.render();
      if (textTotalHeight > h) {
        measuredHeight = textTotalHeight;
        invalidateParentSizeAndDisplayList();
      }
    }
  }

  // todo scroll must work for select text up to end (must autoscroll)
  private function scrollHandler(value:Number):void {
    DataGroup(parent).verticalScrollPosition += value;
  }
}
}

import com.intellij.flex.uiDesigner.css.CssDeclarationImpl;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.flex.CssElementFormat;
import com.intellij.flex.uiDesigner.flex.StylePaneContext;

import flash.display.Graphics;

import flash.display.Shape;
import flash.display.Sprite;
import flash.errors.IllegalOperationError;
import flash.text.engine.FontMetrics;
import flash.text.engine.GroupElement;
import flash.text.engine.TextBlock;
import flash.text.engine.TextElement;
import flash.text.engine.TextLine;

import org.tinytlf.ITextEngine;
import org.tinytlf.decor.ITextDecor;
import org.tinytlf.decor.ITextDecoration;
import org.tinytlf.layout.ConstraintTextContainer;
import org.tinytlf.layout.ITextContainer;

class TextContainer extends ConstraintTextContainer {
  function TextContainer(container:Sprite, explicitWidth:Number) {
    super(container, explicitWidth);
  }
  
  private var _foregroundShape:Shape;
  public function get foregroundShape():Shape {
    return _foregroundShape;
  }

  override public function set target(value:Sprite):void {
    _target = value;
    
    _foregroundShape = new Shape();
    _target.addChild(_foregroundShape);
    
    lines = new Sprite();
    lines.mouseEnabled = false;
    lines.mouseChildren = false;
    _target.addChild(lines);
  }

  override public function resetShapes():void {
    _foregroundShape.graphics.clear();
  }
}

class TextDecoration implements ITextDecor {
  private var shape:Shape;
  private var stylePaneContext:StylePaneContext;
  
  public function TextDecoration(shape:Shape, stylePaneContext:StylePaneContext) {
    this.shape = shape;
    this.stylePaneContext = stylePaneContext;
  }
  
  private var _textEngine:ITextEngine;
  public function get engine():ITextEngine {
   throw new IllegalOperationError();
  }

  public function set engine(value:ITextEngine):void {
    _textEngine = value;
  }

  public function render():void {
    var ruleset:CssRuleset = CssRuleset(_textEngine.blockFactory.data);
    var i:int = 2;
    var n:int = _textEngine.blockFactory.numBlocks - 1 /* close brace */;
    if (ruleset.inline) {
      i = 0;
      n++;
    }

    var rulesets:Vector.<Object> = stylePaneContext.rulesets;
    var rulesetIndex:int = rulesets.indexOf(ruleset);
    assert(rulesetIndex != -1);
    
    var g:Graphics = shape.graphics;
    
    var fontMetrics:FontMetrics = CssElementFormat.defaultText.getFontMetrics();
    g.lineStyle(fontMetrics.strikethroughThickness, 0);
    
    for (; i < n; i++) {
      var textBlock:TextBlock = _textEngine.analytics.getBlockAt(i);
      var declarationName:String = TextElement(GroupElement(textBlock.content).getElementAt(0)).text;
      if (stylePaneContext.styleManager.isInheritingStyle(declarationName)) {
        for (var j:int = 0; j < rulesetIndex; j++) {
          var childRuleset:CssRuleset = rulesets[j] as CssRuleset; // may be group item
          if (childRuleset != null && declarationName in childRuleset.declarationMap) {
            var line:TextLine = textBlock.firstLine;
            do {
              var y:Number = line.y + fontMetrics.strikethroughOffset; // not need to ceil (as for underlineOffset)
              g.moveTo(line.x, y);
              g.lineTo(line.x + line.width, y);
            }
            while ((line = line.nextLine) != null)
          }
        }
      }
    }
  }

  public function removeAll():void {
    throw new IllegalOperationError();
  }

  public function decorate(element:*, styleObject:Object, layer:int = 3, container:ITextContainer = null, foreground:Boolean = false):void {
    throw new IllegalOperationError();
  }

  public function undecorate(element:* = null, decorationProp:String = null):void {
    throw new IllegalOperationError();
  }

  public function mapDecoration(decorationProp:String, decorationClassOrFactory:Object):void {
    throw new IllegalOperationError();
  }

  public function unMapDecoration(decorationProp:String):Boolean {
    throw new IllegalOperationError();
  }

  public function hasDecoration(decorationProp:String):Boolean {
    throw new IllegalOperationError();
  }

  public function getDecoration(decorationProp:String, container:ITextContainer = null):ITextDecoration {
    throw new IllegalOperationError();
  }
}
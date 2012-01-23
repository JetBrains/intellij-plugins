package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import cocoa.Border;
import cocoa.renderer.TextLineAndDisplayObjectEntry;
import cocoa.renderer.TextLineAndDisplayObjectEntryFactory;
import cocoa.renderer.TextLineEntry;
import cocoa.renderer.TextRendererManager;
import cocoa.text.TextFormat;

import com.intellij.flex.uiDesigner.css.CssRuleset;

import flash.desktop.NativeApplication;
import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Shape;
import flash.display.Sprite;
import flash.display.Stage;
import flash.text.engine.TextLine;

import org.tinytlf.ITextEngine;
import org.tinytlf.TextEngine;
import org.tinytlf.layout.TinytlfSprite;

public class StyleRendererManager extends TextRendererManager {
  private const groupTitleEntryFactory:TextLineAndDisplayObjectEntryFactory = new TextLineAndDisplayObjectEntryFactory(Shape, true);
  private var groupBorder:Border;

  private const cssDeclarationViewEntryFactory:TextLineAndDisplayObjectEntryFactory = new TextLineAndDisplayObjectEntryFactory(TinytlfSprite, true);
  private const namespaceViewEntryFactory:TextLineAndDisplayObjectEntryFactory = new TextLineAndDisplayObjectEntryFactory(TinytlfSprite, true);

  protected var titleCanvasContainer:Sprite;
  private var stylePaneContext:StylePaneContext;

  public function StyleRendererManager(textFormat:TextFormat, groupBorder:Border, stylePaneContext:StylePaneContext) {
    super(textFormat, groupBorder.contentInsets);

    this.groupBorder = groupBorder;
    this.stylePaneContext = stylePaneContext;

    registerEntryFactory(groupTitleEntryFactory);
    registerEntryFactory(cssDeclarationViewEntryFactory);
    registerEntryFactory(namespaceViewEntryFactory);
  }

  override public function set container(value:DisplayObjectContainer):void {
    super.container = value;

    if (titleCanvasContainer == null) {
      titleCanvasContainer = new Sprite();
      titleCanvasContainer.mouseEnabled = false;
      titleCanvasContainer.mouseChildren = false;
    }

    value.addChild(titleCanvasContainer);
  }

  override protected function createEntry(itemIndex:int, x:Number, y:Number, w:int, h:int):TextLineEntry {
    var item:Object = _dataSource.getObjectValue(itemIndex);
    var e:TextLineEntry;
    if (item is StyleDeclarationGroupItem) {
      e = createEntryForGroupTitle(StyleDeclarationGroupItem(item), w, x, y);
    }
    else {
      e = createEntryForCss(item, w, x, y);
    }

    return e;
  }

  private function createEntryForCss(item:Object, w:int, x:Number, y:Number):TextLineEntry {
    var e:TextLineAndDisplayObjectEntry = (item is NamespaceUnificator ? namespaceViewEntryFactory : cssDeclarationViewEntryFactory).create(null);
    var displayContainer:TinytlfSprite = TinytlfSprite(e.displayObject);
    var textEngine:ITextEngine = displayContainer.engine;
    var textContainer:TextContainer;

    var n:int = displayContainer.numChildren;
    while (n-- > 0) {
      displayContainer.removeChildAt(n);
    }

    // tinytlf has some issues with measuredHeight/totalHeight, so, we force update
    // don't have time to fix magic issue right now
    //if (textEngine == null) {
    //noinspection ConstantIfStatementJS
    if (true) {
      var stage:Stage = NativeApplication.nativeApplication.activeWindow.stage;
      assert(stage != null);
      textEngine = new TextEngine(stage);
      displayContainer.engine = textEngine;
      textContainer = new TextContainer(displayContainer, w - 10);
      textEngine.layout.addContainer(textContainer);
      textContainer.scrollable = false;

      textEngine.blockFactory = item is NamespaceUnificator ? new NamespaceListTextBlockFactory() : new CssRulesetTextBlockFactory();
      stylePaneContext.rulesetPrinter.interactor.configureTextEngine(textEngine);
      if (item is CssRuleset) {
        textEngine.decor = new TextDecoration(textContainer.foregroundShape, stylePaneContext);
        CssRulesetTextBlockFactory(textEngine.blockFactory).declarationPrinter = stylePaneContext.rulesetPrinter;
      }
    }
    else {
      textContainer = TextContainer(textEngine.layout.containers[0]);
    }

    //noinspection UnnecessaryLocalVariableJS
    var textWidth:int = w - 10;
    if (textContainer.explicitWidth != textContainer.explicitWidth || textContainer.explicitWidth > textWidth) {
      textContainer.explicitWidth = textWidth;
    }

    if (displayContainer.parent != _container) {
      _container.addChild(displayContainer);
    }

    displayContainer.x = x + 5;
    displayContainer.y = y + 5;

    textEngine.blockFactory.data = item;
    textEngine.render();

    _lastCreatedRendererDimension = Math.ceil(textContainer.measuredHeight) + 10;
    return e;
  }

  private function createEntryForGroupTitle(item:StyleDeclarationGroupItem, w:int, x:Number, y:Number):TextLineEntry {
    var text:String;
    if (item.owner == null) {
      text = "Global"
    }
    else {
      if (!("id" in item.owner) || (text = item.owner["id"]) == null) {
        text = item.owner.name;
      }

      text = "Inherited from " + text;
    }

    var line:TextLine = textLineRendererFactory.create(textLineContainer, text, w, textFormat.format);
    layoutTextLine(line, x, y, groupBorder.layoutHeight);
    var e:TextLineAndDisplayObjectEntry = groupTitleEntryFactory.create(line);
    var shape:Shape = Shape(e.displayObject);
    if (shape.parent != titleCanvasContainer) {
      titleCanvasContainer.addChild(shape);
    }

    shape.y = y;
    shape.x = x;
    var g:Graphics = shape.graphics;
    g.clear();
    groupBorder.draw(g, w, groupBorder.layoutHeight);

    _lastCreatedRendererDimension = groupBorder.layoutHeight;
    return e;
  }
}
}

import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.ui.CssElementFormat;
import com.intellij.flex.uiDesigner.ui.inspectors.styleInspector.StylePaneContext;

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
      i = ruleset.file == null ? 0 : 1;
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
      if (stylePaneContext.documentStyleManager.isInheritingStyle(declarationName)) {
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
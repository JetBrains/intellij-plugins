package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import cocoa.text.TextLineUtil;
import cocoa.util.StringUtil;

import com.intellij.flex.uiDesigner.AssetInfo;
import com.intellij.flex.uiDesigner.Module;
import com.intellij.flex.uiDesigner.Server;
import com.intellij.flex.uiDesigner.css.CssCondition;
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssDeclarationImpl;
import com.intellij.flex.uiDesigner.css.CssEmbedAssetDeclaration;
import com.intellij.flex.uiDesigner.css.CssEmbedSwfDeclaration;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.CssSelector;
import com.intellij.flex.uiDesigner.flex.ClassReference;
import com.intellij.flex.uiDesigner.ui.CssElementFormat;

import flash.text.engine.ContentElement;
import flash.text.engine.GroupElement;
import flash.text.engine.TextBlock;
import flash.text.engine.TextElement;
import flash.utils.getQualifiedClassName;

import org.tinytlf.layout.constraints.FloatConstraint;
import org.tinytlf.layout.properties.Insets;
import org.tinytlf.layout.properties.LayoutProperties;
import org.tinytlf.layout.properties.TextFloat;
import org.tinytlf.util.fte.TextBlockUtil;

public class CssRulesetPrinter {
  private const twoSpacePadding:Insets = new Insets();
  
  public function CssRulesetPrinter(interactor:Interactor) {
    _interactor = interactor;

    twoSpacePadding.left = TextLineUtil.measureText("__", CssElementFormat.defaultText).textWidth;
  }
  
  private var _interactor:Interactor;
  public function get interactor():Interactor {
    return _interactor;
  }

  private var _module:Module;
  public function set module(value:Module):void {
    _module = value;
  }
  
  private var _namespaceUnificator:NamespaceUnificator;
  public function set namespaceUnificator(value:NamespaceUnificator):void {
    _namespaceUnificator = value;
  }

  public var availableWidth:Number;

  public function createTextBlock(ruleset:CssRuleset, index:int):TextBlock {
    if (index == 0 && ruleset.file != null) {
      return printFileSource(ruleset);
    }
    
    var block:TextBlock;
    var content:ContentElement;
    // inline
    if (ruleset.inline) {
      var dataIndex:int = ruleset.file == null ? index : index - 1;
      if (dataIndex < ruleset.declarations.length) {
        content = printEntry(ruleset.declarations[dataIndex], ruleset);
      }
    } 
    else if (index == 1) {
      content = createSelector(ruleset);
    }
    else {
      var maxIndex:int = ruleset.declarations.length + 2;
      if (index < maxIndex) {
        block = TextBlockUtil.checkOut();
        block.userData = new LayoutProperties(twoSpacePadding);
        block.content = printEntry(ruleset.declarations[index - 2], ruleset);
        return block;
      }
      else if (index == maxIndex) {
        content = new TextElement("}", CssElementFormat.defaultText);
      }
    }
    
    if (content == null) {
      return null;
    }
    else {
      block = TextBlockUtil.checkOut();
      block.content = content;
      return block;
    }
  }

  private static function printFileSource(ruleset:CssRuleset):TextBlock {
    var block:TextBlock = TextBlockUtil.checkOut();
    var lp:LayoutProperties = new LayoutProperties();
    lp.constraint = new FloatConstraint(TextFloat.RIGHT);
    block.userData = lp;
    
    var url:String = ruleset.file.url;
    var name:String = ruleset.file.name;
    if (StringUtil.startsWith(url, "jar://")) {
      var end:int = url.lastIndexOf("!/");
      name = "[" + url.substring(url.lastIndexOf("/", end - 4) + 1, end) + "] " + name;
    }
    
    var textElement:TextElement = new TextElement(name + ":" + ruleset.line, CssElementFormat.fileLinkHover);
    textElement.userData = ruleset;
    block.content = textElement;
    return block;
  }

  private function createSelector(ruleset:CssRuleset):ContentElement {
    if (ruleset.selectors[0].subject == "global") {
      return new TextElement("{", CssElementFormat.defaultText);
    }
    
    var elementsCounter:int = 0;
    var elements:Vector.<ContentElement> = new Vector.<ContentElement>(2);
    var selectors:Vector.<CssSelector> = ruleset.selectors;
    var i:int = 0, n:int = selectors.length;
    while (true) {
      elementsCounter = printSelector(ruleset, selectors[i++], elements, elementsCounter);
      if (i == n) {
        break;
      }
      else {
        elementsCounter = appendText(", ", elements, elementsCounter);
      }
    }
    
    elements[elementsCounter] = new TextElement(" {", CssElementFormat.defaultText);
    elements.fixed = true;
    return new GroupElement(elements);
  }
  
  private static function appendText(text:String, elements:Vector.<ContentElement>, elementsCounter:int):int {
    var lastElement:TextElement = TextElement(elements[elementsCounter - 1]);
    if (lastElement.userData == null) {
      lastElement.replaceText(lastElement.text.length, lastElement.text.length, text);
    }
    else {
      elements[elementsCounter++] = new TextElement(", ", CssElementFormat.defaultText);
    }
    
    return elementsCounter;
  }
  
  private function printSelector(ruleset:CssRuleset, selector:CssSelector, elements:Vector.<ContentElement>, elementsCounter:int):int {
    if (selector.ancestor != null) {
      elementsCounter = printSelector(ruleset, selector.ancestor, elements, elementsCounter);
      elementsCounter = appendText(" ", elements, elementsCounter);
    }
    
    if (selector.presentableSubject != null) {
      var namespacePrefix:String = _namespaceUnificator.getNamespacePrefix(ruleset, selector);
      if (namespacePrefix != null) {
        elements[elementsCounter++] = new TextElement(namespacePrefix + "|", CssElementFormat.identifier);
      }

      var textElement:TextElement = new TextElement(selector.presentableSubject, CssElementFormat.identifier);
      textElement.userData = selector.subject;
      elements[elementsCounter++] = textElement;
    }
    
    if (selector.conditions != null) {
      var text:String = "";
      for each (var condition:CssCondition in selector.conditions) {
        text = condition.appendString(text);
      }
      
      elements[elementsCounter++] = new TextElement(text, CssElementFormat.identifier);
    }
    
    return elementsCounter;
  }

  private function determinateTypeForExternalInlineStyle(value:*):int {
    if (value === null) {
      return CssPropertyType.NULL;
    }
    else if (value is Boolean) {
      return CssPropertyType.BOOL;
    }
    else if (value is String) {
      return CssPropertyType.STRING;
    }
    else if (value is Number) {
      return CssPropertyType.NUMBER;
    }
    else if (value is Array) {
      return CssPropertyType.ARRAY;
    }
    else if (value is _module.getClass("mx.effects.IEffect")) {
      return CssPropertyType.EFFECT;
    }
    else if (value === undefined) {
      return CssPropertyType.CLEARED;
    }
    else {
      return -1;
    }
  }

  private function printEntry(descriptor:CssDeclaration, ruleset:CssRuleset):GroupElement {
    var content:Vector.<ContentElement>;
    var contentIndex:int = 2;
    var array:Array;
    var maxI:int;
    var i:int;

    // lazy set type for external inline style
    if (descriptor.type == -1) {
      CssDeclarationImpl(descriptor).type = determinateTypeForExternalInlineStyle(descriptor.value);
    }

    var linkableStyle:Boolean = !descriptor.fromAs;
    switch (descriptor.type) {
      case -1:
        if (descriptor.value is Class) {
          content = printClassReference(contentIndex, getQualifiedClassName(descriptor.value).replace("::", "."));
          contentIndex = -1;
        }
        else {
          content = new Vector.<ContentElement>(contentIndex + 2, true);
          content[contentIndex++] = new TextElement(descriptor.value.toString(), CssElementFormat.comment);
          linkableStyle = false;
        }
        break;

      case CssPropertyType.STRING:
        content = new Vector.<ContentElement>(4, true);
        content[contentIndex++] = new TextElement('"' + descriptor.value + '"', CssElementFormat.string);
        break;

      case CssPropertyType.COLOR_INT:
        content = new Vector.<ContentElement>(4, true);
        content[contentIndex++] = new TextElement(intColorToHex(uint(descriptor.value)), CssElementFormat.func);
        break;

      case CssPropertyType.COLOR_STRING:
        content = new Vector.<ContentElement>(4, true);
        content[contentIndex++] = new TextElement(descriptor.colorName, CssElementFormat.string);
        break;

      case CssPropertyType.BOOL:
        content = new Vector.<ContentElement>(4, true);
        content[contentIndex++] = new TextElement(descriptor.value ? "true" : "false", CssElementFormat.func);
        break;

      case CssPropertyType.NUMBER:
        content = new Vector.<ContentElement>(4, true);
        content[contentIndex++] = new TextElement(descriptor.value.toString(), CssElementFormat.number);
        break;

      case CssPropertyType.CLASS_REFERENCE:
        content = printClassReference(contentIndex, ClassReference(descriptor.value).className);
        contentIndex = -1;
        break;

      case CssPropertyType.NULL:
        content = new Vector.<ContentElement>(contentIndex + 4, true);
        content[contentIndex++] = new TextElement("ClassReference", CssElementFormat.func);
        content[contentIndex++] = new TextElement("(", CssElementFormat.defaultText);
        content[contentIndex++] = new TextElement("null", CssElementFormat.func);
        content[contentIndex] = new TextElement(");", CssElementFormat.defaultText);
        contentIndex = -1;
        break;

      case CssPropertyType.ARRAY_OF_COLOR:
        array = descriptor.value as Array;
        maxI = array.length - 1;
        content = new Vector.<ContentElement>(array.length + maxI + 3, true);
        for (i = 0; ; i++) {
          content[contentIndex++] = new TextElement(intColorToHex(uint(array[i])), CssElementFormat.func);
          if (i == maxI) {
            break;
          }
          else {
            content[contentIndex++] = new TextElement(", ", CssElementFormat.defaultText);
          }
        }
        break;

      case CssPropertyType.ARRAY:
        array = descriptor.value as Array;
        if (array.length == 0) {
          content = new Vector.<ContentElement>(contentIndex + 2, true);
          content[contentIndex++] = new TextElement("empty array", CssElementFormat.defaultText);
          linkableStyle = false;
          break;
        }

        maxI = array.length - 1;
        content = new Vector.<ContentElement>(array.length + maxI + 3, true);
        for (i = 0; ; i++) {
          var item:Object = array[i];
          content[contentIndex++] = new TextElement(item.toString(), item is String ? CssElementFormat.string : CssElementFormat.number);
          if (i == maxI) {
            break;
          }
          else {
            content[contentIndex++] = new TextElement(", ", CssElementFormat.defaultText);
          }
        }
        break;

      case CssPropertyType.EMBED:
        content = printEmbed(contentIndex, CssEmbedAssetDeclaration(descriptor));
        contentIndex = -1;
        break;
      
      case CssPropertyType.EFFECT:
        content = new Vector.<ContentElement>(4, true);
        // todo more usefull effect representation
        var effectClassName:String = getQualifiedClassName(descriptor.value);
        content[contentIndex++] = new TextElement(effectClassName.substr(effectClassName.lastIndexOf(":") + 1), CssElementFormat.string);
        break;
      
      case CssPropertyType.CLEARED:
        content = new Vector.<ContentElement>(contentIndex + 2, true);
        content[contentIndex++] = new TextElement("cleared", CssElementFormat.func);
              
        linkableStyle = false;
        break;

      default:
        throw new ArgumentError("unknown type of value: " + descriptor.type);
    }

    var textElement:TextElement = new TextElement(descriptor.presentableName, CssElementFormat.propertyName);
    if (linkableStyle) {
      textElement.userData = ruleset;
    }
    
    content[0] = textElement;
    content[1] = new TextElement(": ", CssElementFormat.defaultText);
    if (contentIndex != -1) {
      content[contentIndex] = new TextElement(";", CssElementFormat.defaultText);
    }
    return new GroupElement(content);
  }

  private static function intColorToHex(color:uint):String {
    var s:String = color.toString(16);
    return "#" + s + StringUtil.repeat("0", 6 - s.length);
  }

  private static function printClassReference(contentIndex:int, name:String):Vector.<ContentElement> {
    var content:Vector.<ContentElement> = new Vector.<ContentElement>(contentIndex + 6, true);
    content[contentIndex++] = new TextElement("ClassReference", CssElementFormat.func);
    content[contentIndex++] = new TextElement("(", CssElementFormat.defaultText);
    content[contentIndex++] = new TextElement('"', CssElementFormat.string);

    var textElement:TextElement = new TextElement(name, CssElementFormat.string);
    textElement.userData = name;
    content[contentIndex++] = textElement;

    content[contentIndex++] = new TextElement('"', CssElementFormat.string);
    content[contentIndex] = new TextElement(");", CssElementFormat.defaultText);

    return content;
  }

  private function printEmbed(contentIndex:int, embedAsset:CssEmbedAssetDeclaration):Vector.<ContentElement> {
    var server:Server = Server(_module.project.getComponent(Server));
    var assetInfo:AssetInfo = server.getAssetInfo(embedAsset.id, _module.project, embedAsset is CssEmbedSwfDeclaration);

    const symbol:String = assetInfo.symbol;
    var content:Vector.<ContentElement> = new Vector.<ContentElement>(contentIndex + (symbol == null ? 6 : 12), true);
    content[contentIndex++] = new TextElement("Embed", CssElementFormat.func);
    content[contentIndex++] = new TextElement("(", CssElementFormat.defaultText);

    if (symbol != null) {
      content[contentIndex++] = new TextElement('source', CssElementFormat.func);
      content[contentIndex++] = new TextElement("=", CssElementFormat.defaultText);
    }

    content[contentIndex++] = new TextElement('"', CssElementFormat.string);
    var textElement:TextElement = new TextElement(assetInfo.file.presentableUrl, CssElementFormat.string);
    textElement.userData = assetInfo.file;
    content[contentIndex++] = textElement;
    content[contentIndex++] = new TextElement('"', CssElementFormat.string);

    if (symbol != null) {
      content[contentIndex++] = new TextElement(", ", CssElementFormat.defaultText);
      content[contentIndex++] = new TextElement('symbol', CssElementFormat.func);
      content[contentIndex++] = new TextElement("=", CssElementFormat.defaultText);
      content[contentIndex++] = new TextElement('"' + symbol + '"', CssElementFormat.string);
    }

    content[contentIndex] = new TextElement(");", CssElementFormat.defaultText);

    return content;
  }
}
}

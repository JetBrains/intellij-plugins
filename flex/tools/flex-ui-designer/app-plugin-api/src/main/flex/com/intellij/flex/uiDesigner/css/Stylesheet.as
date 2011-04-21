package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.Dictionary;
import flash.utils.IDataInput;

public final class Stylesheet {
  private static const cssConditions:Vector.<Class> = new <Class>[CssClassCondition, CssIdCondition, CssPseudoCondition];

  private var _namespaces:Dictionary;
  public function get namespaces():Dictionary {
    return _namespaces;
  }
  
  private var _rulesets:Vector.<CssRuleset>;
  public function get rulesets():Vector.<CssRuleset> {
    return _rulesets;
  }

  public function readExternal(input:IDataInput):void {
    var stringRegistry:StringRegistry = StringRegistry.instance;
    var n:int = AmfUtil.readUInt29(input);
    if (n > 0) {
      _rulesets = new Vector.<CssRuleset>(n, true);
      for (var i:int = 0; i < n; i++) {
        readRuleset(_rulesets[i] = CssRuleset.create(AmfUtil.readUInt29(input), AmfUtil.readUInt29(input)), input, stringRegistry);
      }
    }
    
    n = input.readUnsignedByte();
    if (n > 0) {
      _namespaces = new Dictionary();
      while (n-- > 0) {
        _namespaces[stringRegistry.read(input)] = stringRegistry.read(input);
      }
    }
  }

  private static function readRuleset(ruleset:CssRuleset, input:IDataInput, stringRegistry:StringRegistry):void {
    var i:int;
    const selectorsLength:int = input.readByte();
    var selectors:Vector.<CssSelector> = new Vector.<CssSelector>(selectorsLength, true);
    for (i = 0; i < selectorsLength; i++) {
      selectors[i] = readSimpleSelectors(input, stringRegistry);
    }
    ruleset.selectors = selectors;

    var n:int = AmfUtil.readUInt29(input);
    if (n > 0) {
      var declarations:Vector.<CssDeclaration> = new Vector.<CssDeclaration>(n, true);
      for (i = 0; i < n; i++) {
        var name:String = stringRegistry.read(input);
        var textOffset:int = AmfUtil.readUInt29(input);
        var type:int = input.readByte();
        switch (type) {
          case 8:
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, null, AmfUtil.readUtf(input));
            break;

          case 4:
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, null, new ClassReferenceImpl(stringRegistry.read(input)));
            break;
          
          case 7:
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, null, null);
            break;

          case 5:
            const symbolLength:int = AmfUtil.readUInt29(input);
            CssEmbedSwfDeclaration(declarations[i] = CssEmbedSwfDeclaration.create(name, textOffset, symbolLength == 0 ? null : input.readUTFBytes(symbolLength), AmfUtil.readUInt29(input)));
            break;

          case 10:
            CssEmbedImageDeclaration(declarations[i] = CssEmbedImageDeclaration.create(name, textOffset, AmfUtil.readUInt29(input)));
            break;

          default:
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, type == CssPropertyType.COLOR_STRING ? stringRegistry.read(input) : null, input.readObject());
            break;
        }
      }
      ruleset.declarations = declarations;
    }
  }

  private static function readSimpleSelectors(data:IDataInput, stringRegistry:StringRegistry):CssSelector {
    const simpleSelectorsLength:int = data.readByte();
    var ancestor:CssSelector;
    for (var i:int = 0; i < simpleSelectorsLength; i++) {
      var subject:String = stringRegistry.read(data);
      var presentableSubject:String = subject == null ? null : stringRegistry.read(data);
      var namespacePrefix:String = presentableSubject == null ? null : stringRegistry.read(data);
      var conditionsLength:int = data.readByte();
      var conditions:Vector.<CssCondition> = null;
      if (conditionsLength > 0) {
        conditions = new Vector.<CssCondition>(conditionsLength, true);
        for (var j:int = 0; j < conditionsLength; j++) {
          var clazz:Class = cssConditions[data.readByte()];
          conditions[j] = new clazz(stringRegistry.read(data));
        }
      }

      ancestor = new CssSelector(subject, presentableSubject, namespacePrefix, conditions, ancestor);
    }

    return ancestor;
  }
}
}
package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContextImpl;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.AmfExtendedTypes;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.mxml.MxmlReader;

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

  public function read(input:IDataInput):void {
    var stringRegistry:StringRegistry = StringRegistry.instance;
    var n:int = AmfUtil.readUInt29(input);
    if (n > 0) {
      _rulesets = new Vector.<CssRuleset>(n, true);
      for (var i:int = 0; i < n; i++) {
        var ruleSet: CssRuleset = CssRuleset.create(AmfUtil.readUInt29(input), AmfUtil.readUInt29(input));
        try {
          readRuleset(_rulesets[i] = ruleSet, input, stringRegistry);
        }
        catch (e:Error) {
          throw new Error("Cannot read css ruleset (line: " + ruleSet.line + "): " + e.message +  ", rulesets " + _rulesets + "\n" + e.getStackTrace())
        }
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
      try {
        selectors[i] = readSimpleSelectors(input, stringRegistry);
      }
      catch (e:Error) {
        throw new Error("Cannot read css selectors: " + e.message +  ", selectors " + selectors + "\n" + e.getStackTrace())
      }
    }
    ruleset.selectors = selectors;

    var n:int = AmfUtil.readUInt29(input);
    if (n > 0) {
      var declarations:Vector.<CssDeclaration> = new Vector.<CssDeclaration>(n, true);
      for (i = 0; i < n; i++) {
        var name:String = stringRegistry.read(input);
        var textOffset:int = AmfUtil.readUInt29(input);
        var type:int = input.readByte();
        try {
          var v:Object = MxmlReader.readPrimitive(type, input, stringRegistry);
          if (v == input) {
            switch (type) {
              case AmfExtendedTypes.CLASS_REFERENCE:
                declarations[i] = CssDeclarationImpl.create2(CssPropertyType.CLASS_REFERENCE, name, textOffset, null, new ClassReferenceImpl(stringRegistry.readNotNull(input)));
                break;

              case AmfExtendedTypes.SWF:
                CssEmbedSwfDeclaration(declarations[i] = CssEmbedSwfDeclaration.create2(name, textOffset, input));
                break;

              case AmfExtendedTypes.IMAGE:
                CssEmbedImageDeclaration(declarations[i] = CssEmbedImageDeclaration.create(name, textOffset, AmfUtil.readUInt29(input)));
                break;

              case AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE:
                declarations[i] = readSkinClass(textOffset, input);
                break;

              case Amf3Types.ARRAY:
                declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, null, readCssArray(input, stringRegistry));
                break;

              default:
                declarations[i] =
                  CssDeclarationImpl.create2(type, name, textOffset, type == CssPropertyType.COLOR_STRING ? stringRegistry.readNotNull(input) : null, input.readObject());
                break;
            }
          }
          else {
            if (type == Amf3Types.TRUE || type == Amf3Types.FALSE) {
              type = CssPropertyType.BOOL;
            }
            else if (type == Amf3Types.INTEGER || type == Amf3Types.DOUBLE) {
              type = CssPropertyType.NUMBER;
            }
            else if (type == AmfExtendedTypes.STRING_REFERENCE) {
              type = Amf3Types.STRING;
            }
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, null, v);
          }
        }
        catch (e:Error) {
          throw new Error("Cannot read css declaration " + name + ": " + e.message + "\n" + e.getStackTrace())
        }
      }
      ruleset.declarations = declarations;
    }
  }

  private static function readCssArray(input:IDataInput, stringRegistry:StringRegistry): Array {
    const length:int = input.readUnsignedByte();
    var result: Array = new Array(length)
    var i:int = 0;
    while (i < length) {
      result[i++] = MxmlReader.readPrimitive(input.readByte(), input, stringRegistry)
    }
    return result;
  }

  private static function readSkinClass(textOffset:int, input:IDataInput):CssDeclaration {
    const id:int = AmfUtil.readUInt29(input);
    var documentFactory:DocumentFactory = DocumentFactoryManager.getInstance().getById(id);
    var flexLibrarySet:FlexLibrarySet = documentFactory.module.flexLibrarySet;
    var factory:Object = flexLibrarySet.getDocumentFactory(id);
    if (factory == null) {
      factory = new flexLibrarySet.documentFactoryClass(documentFactory, new DeferredInstanceFromBytesContextImpl(documentFactory,
                                                                                                                  documentFactory.module.styleManager));
      flexLibrarySet.putDocumentFactory(id, factory);
    }

    return new CssSkinClassDeclaration(factory, textOffset);
  }

  private static function readSimpleSelectors(data:IDataInput, stringRegistry:StringRegistry):CssSelector {
    const simpleSelectorsLength:int = data.readByte();
    var ancestor:CssSelector = null;
    for (var i:int = 0; i < simpleSelectorsLength; i++) {
      var subject:String = stringRegistry.read(data);
      var presentableSubject:String = subject == null ? null : stringRegistry.read(data);
      var namespacePrefix:String = presentableSubject == null ? null : stringRegistry.read(data);

      var conditionsLength:int = data.readByte();
      if (conditionsLength > 0) {
        try {
          var conditions:Vector.<CssCondition> = null;
          conditions = new Vector.<CssCondition>(conditionsLength, true);
          for (var j:int = 0; j < conditionsLength; j++) {
            var clazz:Class = cssConditions[data.readByte()];
            conditions[j] = new clazz(stringRegistry.read(data));
          }
        }
        catch (e:Error) {
          throw new Error("Cannot read css conditions " + subject + " (conditions length: " + conditionsLength + "): " + e.message + "\n" + e.getStackTrace())
        }
      }

      ancestor = new CssSelector(subject, presentableSubject, namespacePrefix, conditions, ancestor);
    }

    return ancestor;
  }
}
}
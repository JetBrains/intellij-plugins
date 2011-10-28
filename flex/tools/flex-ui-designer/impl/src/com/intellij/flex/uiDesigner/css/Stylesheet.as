package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.DocumentFactory;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.DocumentReader;
import com.intellij.flex.uiDesigner.ModuleContextEx;
import com.intellij.flex.uiDesigner.Project;
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;
import com.intellij.flex.uiDesigner.io.AmfExtendedTypes;
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

  public function read(input:IDataInput, project:Project):void {
    var stringRegistry:StringRegistry = StringRegistry.instance;
    var n:int = AmfUtil.readUInt29(input);
    if (n > 0) {
      _rulesets = new Vector.<CssRuleset>(n, true);
      for (var i:int = 0; i < n; i++) {
        readRuleset(_rulesets[i] = CssRuleset.create(AmfUtil.readUInt29(input), AmfUtil.readUInt29(input)), input, stringRegistry, project);
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

  private static function readRuleset(ruleset:CssRuleset, input:IDataInput, stringRegistry:StringRegistry, project:Project):void {
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
          case CssPropertyType.STRING:
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, null, AmfUtil.readString(input));
            break;

          case AmfExtendedTypes.STRING_REFERENCE:
            declarations[i] = CssDeclarationImpl.create2(CssPropertyType.STRING, name, textOffset, null, stringRegistry.readNotNull(input));
            break;

          case AmfExtendedTypes.CLASS_REFERENCE:
            declarations[i] = CssDeclarationImpl.create2(CssPropertyType.CLASS_REFERENCE, name, textOffset, null, new ClassReferenceImpl(stringRegistry.readNotNull(input)));
            break;
          
          case CssPropertyType.NULL:
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, null, null);
            break;

          case CssPropertyType.NAN:
            declarations[i] = CssDeclarationImpl.create2(CssPropertyType.NUMBER, name, textOffset, null, NaN);
            break;

          case AmfExtendedTypes.SWF:
            CssEmbedSwfDeclaration(declarations[i] = CssEmbedSwfDeclaration.create2(name, textOffset, input));
            break;

          case AmfExtendedTypes.IMAGE:
            CssEmbedImageDeclaration(declarations[i] = CssEmbedImageDeclaration.create(name, textOffset, AmfUtil.readUInt29(input)));
            break;

          case AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE:
            declarations[i] = readSkinClass(textOffset, input, project);
            break;

          default:
            declarations[i] = CssDeclarationImpl.create2(type, name, textOffset, type == CssPropertyType.COLOR_STRING ? stringRegistry.readNotNull(input) : null, input.readObject());
            break;
        }
      }
      ruleset.declarations = declarations;
    }
  }

  private static function readSkinClass(textOffset:int, input:IDataInput, project:Project):CssDeclaration {
    const id:int = AmfUtil.readUInt29(input);
    var documentFactory:DocumentFactory = DocumentFactoryManager.getInstance(project).get(id);
    var moduleContext:ModuleContextEx = documentFactory.module.context;
    var factory:Object = moduleContext.getDocumentFactory(id);
    if (factory == null) {
      factory = new moduleContext.documentFactoryClass(documentFactory,
          new DeferredInstanceFromBytesContext(documentFactory, DocumentReader(project.getComponent(DocumentReader)), moduleContext.styleManager));
      moduleContext.putDocumentFactory(id, factory);
    }

    return new CssSkinClassDeclaration(factory, textOffset);
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
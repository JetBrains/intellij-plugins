package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;
import flash.utils.getQualifiedClassName;

public class CssRuleset implements IExternalizable {
  private static const cssConditions:Vector.<Class> = new <Class>[CssClassCondition, CssIdCondition, CssPseudoCondition];
  
  private var _selectors:Vector.<CssSelector>;
  public function get selectors():Vector.<CssSelector> {
    return _selectors;
  }
  
  public function get inline():Boolean {
    return false;
  }

  protected var _declarationMap:Dictionary;
  public function get declarationMap():Dictionary {
    if (_declarationMap == null) {
      _declarationMap = new Dictionary();
      for each (var declaration:CssDeclaration in declarations) {
        _declarationMap[declaration.name] = declaration;
      }
    }
    
    return _declarationMap;
  }

  protected var _declarations:Vector.<CssDeclaration>;
  public function get declarations():Vector.<CssDeclaration> {
    return _declarations;
  }

  protected var _file:VirtualFile;
  public function get file():VirtualFile {
    return _file;
  }
  public function set file(file:VirtualFile):void {
    _file = file;
  }

  protected var _line:int;
  public function get line():int {
    return _line;
  }

  protected var _textOffset:int = -1;
  public function get textOffset():int {
    return _textOffset;
  }

  public function put(name:String, value:*):void {
    var propertyDescriptor:CssDeclarationImpl = _declarationMap == null ? null : _declarationMap[name];
    if (propertyDescriptor == null) {
      propertyDescriptor = CssDeclarationImpl.createRuntime(name, value, true);
      if (_declarations.fixed) {
        // we don't restore fixed after — if anybody put, so, it is never fixed
        _declarations.fixed = false;
      }
      _declarations[_declarations.length] = propertyDescriptor;

      if (_declarationMap != null) {
        _declarationMap[name] = propertyDescriptor;
      }
    }
    else {
      if (propertyDescriptor.type != -1) {
        if (getQualifiedClassName(propertyDescriptor.value) != getQualifiedClassName(value)) {
          propertyDescriptor.type = -1;
        }
      }

      propertyDescriptor.value = value;
    }
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    _line = AmfUtil.readUInt29(input);
    _textOffset = AmfUtil.readUInt29(input);
    
    var stringRegistry:StringRegistry = StringRegistry.instance;
    const selectorsLength:int = input.readByte();
    _selectors = new Vector.<CssSelector>(selectorsLength, true);
    for (var i:int = 0; i < selectorsLength; i++) {
      _selectors[i] = readSimpleSelectors(input, stringRegistry);
    }

    _declarations = input.readObject();
  }
  
  private function readSimpleSelectors(data:IDataInput, stringRegistry:StringRegistry):CssSelector {
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
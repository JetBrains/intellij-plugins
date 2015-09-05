package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.VirtualFile;

import flash.utils.Dictionary;
import flash.utils.getQualifiedClassName;

public class CssRuleset {
  public static const TEXT_OFFSET_UNDEFINED:int = -1;
  public static const GUESS_TEXT_OFFSET_BY_PARENT:int = -2;

  public var declarations:Vector.<CssDeclaration>;

  public static function create(line:int, textOffset:int):CssRuleset {
     var ruleset:CssRuleset = new CssRuleset();
    ruleset._line = line;
    ruleset._textOffset = textOffset;
    return ruleset;
  }

  public var selectors:Vector.<CssSelector>;
  
  public function get inline():Boolean {
    return false;
  }

  public function get runtime():Boolean {
    return inline && file == null &&  _textOffset == TEXT_OFFSET_UNDEFINED;
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

  protected var _textOffset:int = CssRuleset.TEXT_OFFSET_UNDEFINED;
  public function get textOffset():int {
    return _textOffset;
  }

  public function put(name:String, value:*):void {
    var propertyDescriptor:CssDeclarationImpl = _declarationMap == null ? null : _declarationMap[name];
    if (propertyDescriptor == null) {
      propertyDescriptor = CssDeclarationImpl.createRuntime(name, value, true);
      if (declarations.fixed) {
        // we don't restore fixed after — if anybody put, so, it is never fixed
        declarations.fixed = false;
      }
      declarations[declarations.length] = propertyDescriptor;

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
}
}
package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public final class CssDeclarationImpl implements IExternalizable, CssDeclaration {
  public function CssDeclarationImpl(fromAs:Boolean = false) {
    _fromAs = fromAs;
  }
  
  public static function create(name:String, textOffset:int):CssDeclarationImpl {
    var declaration:CssDeclarationImpl = new CssDeclarationImpl(false);
    declaration._name = name;
    declaration._textOffset = textOffset;
    return declaration;
  }
  
  public static function createRuntime(name:String, value:Object, fromAs:Boolean):CssDeclarationImpl {
    var declaration:CssDeclarationImpl = new CssDeclarationImpl(fromAs);
    declaration._name = name;
    declaration._value = value;
    return declaration;
  }
  
  private var _fromAs:Boolean;
  public function get fromAs():Boolean {
    return _fromAs;
  }

  private var _name:String;
  public function get name():String {
    return _name;
  }
  
  public function get presentableName():String {
    return _name;
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    var stringRegistry:StringRegistry = StringRegistry.instance;
    _name = stringRegistry.read(input);
    _textOffset = AmfUtil.readUInt29(input);
    type = input.readByte();

    if (_type == CssDeclarationType.COLOR_STRING) {
      _colorName = stringRegistry.read(input);
    }

    _value = input.readObject();
  }
  
  private var _type:int = -1;
  public function get type():int {
    return _type;
  }
  public function set type(value:int):void {
    _type = value;
  }

  private var _value:*;
  public function get value():* {
    return _value;
  }
  public function set value(value:*):void {
    _value = value;
  }

  // we can't determine color name in runtime — example: fuchsia and magenta == 0xFF00FF
  private var _colorName:String;
  public function get colorName():String {
    return _colorName;
  }
  public function set colorName(value:String):void {
    _colorName = value;
  }

  private var _textOffset:int;
  public function get textOffset():int {
    return _textOffset;
  }
}
}
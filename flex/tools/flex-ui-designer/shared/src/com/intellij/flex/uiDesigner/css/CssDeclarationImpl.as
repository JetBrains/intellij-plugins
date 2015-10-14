package com.intellij.flex.uiDesigner.css {
public final class CssDeclarationImpl extends AbstractCssDeclaration implements CssDeclaration {
  public function CssDeclarationImpl(fromAs:Boolean = false) {
    _fromAs = fromAs;
  }
  
  public static function create(name:String, textOffset:int):CssDeclarationImpl {
    var declaration:CssDeclarationImpl = new CssDeclarationImpl(false);
    declaration._name = name;
    declaration._textOffset = textOffset;
    return declaration;
  }

  public static function create2(type:int, name:String, textOffset:int, colorName:String, value:Object):CssDeclarationImpl {
    var declaration:CssDeclarationImpl = new CssDeclarationImpl(false);
    declaration._type = type;
    declaration._name = name;
    declaration._textOffset = textOffset;
    declaration.colorName = colorName;
    declaration._value = value;
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
}
}
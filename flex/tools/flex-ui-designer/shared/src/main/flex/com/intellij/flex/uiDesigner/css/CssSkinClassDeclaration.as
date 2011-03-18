package com.intellij.flex.uiDesigner.css {
public class CssSkinClassDeclaration implements CssDeclaration {
  public function CssSkinClassDeclaration(flexDocumentFactory:Object, textOffset:int) {
    _value = flexDocumentFactory;
    _textOffset = textOffset;
  }

  private var _textOffset:int;
  public function get textOffset():int {
    return _textOffset;
  }

  public function get fromAs():Boolean {
    return false;
  }
  
  public function get name():String {
    return "skinFactory";
  }

  public function get presentableName():String {
    return "skinClass";
  }

  public function get type():int {
    return CssDeclarationType.CLASS_REFERENCE;
  }

  private var _value:Object;
  public function get value():* {
    return _value;
  }

  public function get colorName():String {
    return null;
  }
}
}

package com.intellij.flex.uiDesigner.css {
[Abstract]
public class CssEmbedAssetDeclaration extends AbstractCssDeclaration {
  public var id:int;

  protected var _name:String;
  public function get name():String {
    return _name;
  }

  public function get presentableName():String {
    return _name;
  }

  public function get fromAs():Boolean {
    return false;
  }

  public function get value():* {
    return this;
  }

  public function get type():int {
    return CssPropertyType.EMBED;
  }

  public function get colorName():String {
    return null;
  }
}
}
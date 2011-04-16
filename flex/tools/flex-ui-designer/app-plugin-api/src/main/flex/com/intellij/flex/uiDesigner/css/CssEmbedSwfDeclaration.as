package com.intellij.flex.uiDesigner.css {
public class CssEmbedSwfDeclaration extends CssEmbedAssetDeclaration implements CssDeclaration {
  public var symbol:String;

  public static function create(name:String, textOffset:int, symbol:String, id:int):CssEmbedSwfDeclaration {
    var declaration:CssEmbedSwfDeclaration = new CssEmbedSwfDeclaration();
    declaration._name = name;
    declaration._textOffset = textOffset;
    declaration.id = id;
    declaration.symbol = symbol;
    return declaration;
  }
}
}

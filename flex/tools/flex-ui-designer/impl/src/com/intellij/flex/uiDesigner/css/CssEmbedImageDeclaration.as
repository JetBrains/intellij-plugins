package com.intellij.flex.uiDesigner.css {
public class CssEmbedImageDeclaration extends CssEmbedAssetDeclaration implements CssDeclaration {
  public static function create(name:String, textOffset:int, id:int):CssEmbedImageDeclaration {
    var declaration:CssEmbedImageDeclaration = new CssEmbedImageDeclaration();
    declaration._name = name;
    declaration._textOffset = textOffset;
    declaration.id = id;
    return declaration;
  }
}
}

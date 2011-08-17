package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.IDataInput;

public class CssEmbedSwfDeclaration extends CssEmbedAssetDeclaration implements CssDeclaration {
  public static function create2(name:String, textOffset:int, input:IDataInput):CssEmbedSwfDeclaration {
    return new CssEmbedSwfDeclaration(name, textOffset, AmfUtil.readUInt29(input));
  }

  public function CssEmbedSwfDeclaration(name:String, textOffset:int, id:int) {
    _name = name;
    _textOffset = textOffset;
    this.id = id;
  }
}
}

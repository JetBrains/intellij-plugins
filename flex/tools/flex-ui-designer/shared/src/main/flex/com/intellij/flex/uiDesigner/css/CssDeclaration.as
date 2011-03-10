package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public final class CssDeclaration implements IExternalizable {
  public var name:String;
  public var textOffset:int;
  public var type:int = -1;
  public var value:*;

  // we can't determine color name in runtime — example: fuchsia and magenta == 0xFF00FF
  public var colorName:String;
  
  public var fromAs:Boolean;

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    var stringRegistry:StringRegistry = StringRegistry.instance;
    name = stringRegistry.read(input);
    textOffset = AmfUtil.readUInt29(input);
    type = input.readByte();

    if (type == CssPropertyType.COLOR_STRING) {
      colorName = stringRegistry.read(input);
    }

    value = input.readObject();
  }
}
}
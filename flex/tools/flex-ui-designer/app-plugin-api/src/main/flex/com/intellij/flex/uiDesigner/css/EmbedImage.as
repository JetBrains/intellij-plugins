package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public class EmbedImage implements IExternalizable {
  private var id:int;
  public function EmbedImage() {
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    id = AmfUtil.readUInt29(input);
  }
}
}

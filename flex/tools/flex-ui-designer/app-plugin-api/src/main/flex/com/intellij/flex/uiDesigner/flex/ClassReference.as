package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.StringRegistry;

import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public class ClassReference implements IExternalizable {
  public var className:String;

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    className = StringRegistry.instance.read(input);
  }
}
}
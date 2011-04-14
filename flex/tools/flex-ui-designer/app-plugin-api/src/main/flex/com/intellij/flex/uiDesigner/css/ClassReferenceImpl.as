package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.flex.ClassReference;

import flash.utils.IDataInput;
import flash.utils.IDataOutput;
import flash.utils.IExternalizable;

public class ClassReferenceImpl implements IExternalizable, ClassReference {
  private var _className:String;
   public function get className():String {
    return _className;
  }

  public function writeExternal(output:IDataOutput):void {
  }

  public function readExternal(input:IDataInput):void {
    _className = StringRegistry.instance.read(input);
  }
}
}
package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.flex.DocumentReader;

import flash.utils.IDataInput;

public interface Reader {
  function readExternal(input:IDataInput, reader:DocumentReader):void;
}
}

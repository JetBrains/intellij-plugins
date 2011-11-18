package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.utils.IDataInput;

public interface DocumentReader {
  function read(input:IDataInput, documentReaderContext:DocumentReaderContext, styleManager:StyleManagerEx):Object;
  
  function readDeferredInstanceFromBytes(input:IDataInput, factoryContext:DeferredInstanceFromBytesContext):Object;

  function getObjectTableForDeferredInstanceFromBytes():Vector.<Object>;
}
}

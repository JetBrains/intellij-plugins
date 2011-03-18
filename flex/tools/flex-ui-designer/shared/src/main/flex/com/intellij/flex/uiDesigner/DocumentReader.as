package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.flex.*;
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

public interface DocumentReader {
  function read(input:IDataInput, documentReaderContext:DocumentReaderContext, styleManager:StyleManagerEx):Object;
  
  function read2(input:IDataInput, factoryContext:DeferredInstanceFromBytesContext):Object;

  function createDeferredMxContainersChildren(applicationDomain:ApplicationDomain):void;

  function getLocalObjectTable():Vector.<Object>;
}
}

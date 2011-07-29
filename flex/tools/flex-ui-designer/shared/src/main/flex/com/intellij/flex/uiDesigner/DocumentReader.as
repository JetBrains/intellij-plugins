package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

public interface DocumentReader {
  function read(input:IDataInput, documentReaderContext:DocumentReaderContext):Object;
  
  function read2(input:IDataInput, factoryContext:DeferredInstanceFromBytesContext):Object;

  function createDeferredMxContainersChildren(applicationDomain:ApplicationDomain):void;

  function getLocalObjectTable():Vector.<Object>;
}
}

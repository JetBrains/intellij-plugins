package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

public interface DocumentReader {
  function read(input:IDataInput, documentReaderContext:DocumentReaderContext,
                restorePrevContextAfterRead:Boolean = false):Object;
  
  function readDeferredInstanceFromBytes(input:IDataInput, factoryContext:DeferredInstanceFromBytesContext):Object;

  function createDeferredMxContainersChildren(applicationDomain:ApplicationDomain):void;

  function getObjectTableForDeferredInstanceFromBytes():Vector.<Object>;
}
}

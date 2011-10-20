package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

public interface DocumentReader {
  function read(input:IDataInput, documentReaderContext:DocumentReaderContext, styleManager:StyleManagerEx,
                restorePrevContextAfterRead:Boolean = false):Object;
  
  function readDeferredInstanceFromBytes(input:IDataInput, factoryContext:DeferredInstanceFromBytesContext):Object;

  function createDeferredMxContainersChildren(systemManager:ApplicationDomain):void;

  function getObjectTableForDeferredInstanceFromBytes():Vector.<Object>;
}
}

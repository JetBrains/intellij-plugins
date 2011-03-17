package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReaderContext;

import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

public interface DocumentReader {
  function read(input:IDataInput, documentReaderContext:DocumentReaderContext, styleManager:Object):Object;
  
  function read2(input:IDataInput, context:DeferredInstanceFromBytesContext, readStates:Boolean):Object;

  function createDeferredMxContainersChildren(applicationDomain:ApplicationDomain):void;

  function getLocalObjectTable():Vector.<Object>;
}
}

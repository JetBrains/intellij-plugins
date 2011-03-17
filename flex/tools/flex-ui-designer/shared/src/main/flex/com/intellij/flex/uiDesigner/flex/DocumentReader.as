package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.VirtualFile;

import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

public interface DocumentReader {
  function read(input:IDataInput, documentFile:VirtualFile, styleManager:Object, context:ModuleContext):Object;
  
  function read2(input:IDataInput, context:DeferredInstanceFromBytesContext):Object;

  function createDeferredMxContainersChildren(applicationDomain:ApplicationDomain):void;

  function getLocalObjectTable():Vector.<Object>;
}
}

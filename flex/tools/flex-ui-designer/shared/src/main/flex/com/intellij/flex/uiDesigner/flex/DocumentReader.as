package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.VirtualFile;

import flash.system.ApplicationDomain;
import flash.utils.ByteArray;
import flash.utils.IDataInput;

public interface DocumentReader {
  function read(documentFile:VirtualFile, styleManager:Object, context:ModuleContext):Object;
  
  function read2(bytes:ByteArray, context:DeferredInstanceFromBytesContext):Object;

  function createDeferredMxContainersChildren(applicationDomain:ApplicationDomain):void;

  function getLocalObjectTable():Vector.<Object>;

  function set input(value:IDataInput):void;
}
}

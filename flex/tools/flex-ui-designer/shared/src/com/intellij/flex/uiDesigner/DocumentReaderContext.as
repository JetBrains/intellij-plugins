package com.intellij.flex.uiDesigner {
public interface DocumentReaderContext {
  function get file():VirtualFile;
  
  function get moduleContext():ModuleContext;

  function registerComponentDeclarationRangeMarkerId(component:Object, id:int):void;

  function registerObjectWithId(id:String, object:Object):void;

  function get instanceForRead():Object;
}
}

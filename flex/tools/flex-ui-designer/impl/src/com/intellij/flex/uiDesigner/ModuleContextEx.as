package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;

public interface ModuleContextEx extends ModuleContext {
  function get librarySets():Vector.<LibrarySet>;

  function get project():Project;

  function get librariesResolved():Boolean;

  function getClassPool(id:String):ClassPool;

  function get flexLibrarySet():FlexLibrarySet;
}
}

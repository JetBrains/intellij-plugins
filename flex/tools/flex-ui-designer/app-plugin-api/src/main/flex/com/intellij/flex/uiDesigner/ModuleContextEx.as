package com.intellij.flex.uiDesigner {
public interface ModuleContextEx extends ModuleContext {
  function get librarySets():Vector.<LibrarySet>;

  function get project():Project;

  function get documentFactoryManager():DocumentFactoryManager;
}
}

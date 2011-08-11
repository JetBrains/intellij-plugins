package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.libraries.LibrarySet;

public interface ModuleContextEx extends ModuleContext {
  function get librarySets():Vector.<LibrarySet>;

  function get project():Project;

  function get librariesResolved():Boolean;
  function set librariesResolved(value:Boolean):void;

  function get imageAssetContainerClassPool():AssetContainerClassPool;

  function get swfAssetContainerClassPool():AssetContainerClassPool;
}
}

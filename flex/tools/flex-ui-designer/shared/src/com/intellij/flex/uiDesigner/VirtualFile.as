package com.intellij.flex.uiDesigner {
public interface VirtualFile {
  function get name():String;

  function createChild(name:String):VirtualFile;

  function get url():String;

  function get presentableUrl():String;
}
}

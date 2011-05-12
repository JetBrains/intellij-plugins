package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public interface LibrarySetItem {
  function get path():String;

  function get inheritingStyles():Dictionary;
  function get defaultsStyle():Stylesheet;

  function get file():VirtualFile;

  function get parents():Vector.<LibrarySetItem>;

  function get successors():Vector.<LibrarySetItem>;

  function addSuccessor(successor:LibrarySetItem):void;

  function get loadState():int;
  function set loadState(value:int):void;

  function get filtered():Boolean;
}
}
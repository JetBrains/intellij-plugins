package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public interface Library {
  function get path():String;

  function get inheritingStyles():Dictionary;
  function get defaultsStyle():Stylesheet;

  function get file():VirtualFile;

  function get parents():Vector.<Library>;

  function get successors():Vector.<Library>;

  function addSuccessor(successor:Library):void;

  function get loadState():int;
  function set loadState(value:int):void;
}
}
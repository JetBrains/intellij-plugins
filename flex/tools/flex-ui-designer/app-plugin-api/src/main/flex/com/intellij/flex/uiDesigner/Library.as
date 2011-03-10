package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public interface Library {
  function get path():String;

  function get inheritingStyles():Dictionary;
  function get defaultsStyle():Stylesheet;

  function get file():VirtualFile;
}
}
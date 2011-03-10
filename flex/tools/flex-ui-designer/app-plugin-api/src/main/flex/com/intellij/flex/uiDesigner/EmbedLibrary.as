package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssRuleset;

import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public class EmbedLibrary implements Library {
  private var _path:String;
  
  public function EmbedLibrary(path:String) {
    _path = path;
  }

  public function get path():String {
    return _path;
  }

  public function get inheritingStyles():Dictionary {
    return null;
  }

  public function get defaultsStyle():Stylesheet {
    return null;
  }

  public function get file():VirtualFile {
    return null;
  }
}
}

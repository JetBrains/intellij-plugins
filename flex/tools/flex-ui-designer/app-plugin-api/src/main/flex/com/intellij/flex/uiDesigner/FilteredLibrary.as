package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public class FilteredLibrary implements Library {
  public var origin:OriginalLibrary;

  public function get path():String {
    return origin.path;
  }

  public function get inheritingStyles():Dictionary {
    return origin.inheritingStyles;
  }

  public function get defaultsStyle():Stylesheet {
    return origin.defaultsStyle;
  }

  public function get file():VirtualFile {
    return origin.file;
  }
}
}
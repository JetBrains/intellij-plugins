package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public class Library {
  public function Library(file:VirtualFile, inheritingStyles:Dictionary, defaultsStyle:Stylesheet) {
    _file = file;
    _inheritingStyles = inheritingStyles;
    _defaultsStyle = defaultsStyle;
  }

  private var _file:VirtualFile;
  public function get file():VirtualFile {
    return _file;
  }

  private var _inheritingStyles:Dictionary;
  public function get inheritingStyles():Dictionary {
    return _inheritingStyles;
  }

  private var _defaultsStyle:Stylesheet;
  public function get defaultsStyle():Stylesheet {
    return _defaultsStyle;
  }
}
}
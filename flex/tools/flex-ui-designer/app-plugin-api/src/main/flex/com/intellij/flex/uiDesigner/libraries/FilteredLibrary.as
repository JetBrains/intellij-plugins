package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public class FilteredLibrary extends AbstractLibrary implements Library {
  public function FilteredLibrary(parents:Vector.<Library>) {
    super(parents);
  }
  
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
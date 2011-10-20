package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.utils.Dictionary;

public class LibrarySetFileItem extends AbstractLibrarySetItem implements LibrarySetItem {
  private var library:Library;

  public function LibrarySetFileItem(library:Library, parents:Vector.<LibrarySetItem>, filtered:Boolean) {
    super(parents);
    this.library = library;
    _filtered = filtered;
  }

  private var _filtered:Boolean;
  public function get filtered():Boolean {
    return _filtered;
  }

  private var _successors:Vector.<LibrarySetItem>;
  public function get successors():Vector.<LibrarySetItem> {
    return _successors;
  }

  public function addSuccessor(successor:LibrarySetItem):void {
    if (_successors == null) {
      _successors = new Vector.<LibrarySetItem>();
    }

    _successors[_successors.length] = successor;
  }

  public function get path():String {
    return library.path;
  }

  public function get inheritingStyles():Dictionary {
    return library.inheritingStyles;
  }

  public function get defaultsStyle():Stylesheet {
    return library.defaultsStyle;
  }

  public function get file():VirtualFile {
    return library.file;
  }
}
}
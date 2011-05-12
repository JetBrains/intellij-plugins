package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.errors.IllegalOperationError;
import flash.utils.Dictionary;

public class LibrarySetEmbedItem extends AbstractLibrarySetItem implements LibrarySetItem {
  public function LibrarySetEmbedItem(parent:LibrarySetItem, path:String) {
    super(new <LibrarySetItem>[parent]);
    _path = path;

    parent.addSuccessor(this);
  }

  private var _path:String;
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

  public function get successors():Vector.<LibrarySetItem> {
    return null;
  }

  public function addSuccessor(successor:LibrarySetItem):void {
    throw new IllegalOperationError();
  }

  public function get filtered():Boolean {
    return false;
  }
}
}
package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.Stylesheet;

import flash.errors.IllegalOperationError;
import flash.utils.Dictionary;

public class EmbedLibrary implements Library {
  private var _parents:Vector.<Library>;

  public function EmbedLibrary(parent:Library, path:String) {
    _parents = new <Library>[parent];
    _path = path;

    parent.addSuccessor(this);
  }

  private var _parent:Library;
  //public function get parent():Library {
  //  return _parent;
  //}

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

  public function get parents():Vector.<Library> {
    return _parents;
  }

  public function get successors():Vector.<Library> {
    return null;
  }

  public function addSuccessor(successor:Library):void {
    throw new IllegalOperationError();
  }

  private var _loadState:int;
  public function get loadState():int {
    return _loadState;
  }
  public function set loadState(value:int):void {
    _loadState = value;
  }
}
}

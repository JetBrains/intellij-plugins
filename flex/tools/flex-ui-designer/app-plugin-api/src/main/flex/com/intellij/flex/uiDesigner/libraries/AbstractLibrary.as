package com.intellij.flex.uiDesigner.libraries {
[Abstract]
internal class AbstractLibrary {
  public function AbstractLibrary(parents:Vector.<Library>) {
    _parents = parents;
  }

  private var _parents:Vector.<Library>;
  public function get parents():Vector.<Library> {
    return _parents;
  }

  private var _successors:Vector.<Library>;
  public function get successors():Vector.<Library> {
    return _successors;
  }

  public function addSuccessor(successor:Library):void {
    if (_successors == null) {
      _successors = new Vector.<Library>();
    }

    _successors[_successors.length] = successor;
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

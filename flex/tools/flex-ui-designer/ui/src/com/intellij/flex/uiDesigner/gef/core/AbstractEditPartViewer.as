package com.intellij.flex.uiDesigner.gef.core {
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;

[Abstract]
public class AbstractEditPartViewer implements IEditPartViewer {
  private var _editDomain:EditDomain;
  public function get editDomain():EditDomain {
    return _editDomain;
  }

  public function set editDomain(value:EditDomain):void {
    _editDomain = value;
  }

  public function get rootFigure():DisplayObjectContainer {
    throw new Error("Abstract");
  }

  public function get control():DisplayObject {
    throw new Error("Abstract");
  }
}
}

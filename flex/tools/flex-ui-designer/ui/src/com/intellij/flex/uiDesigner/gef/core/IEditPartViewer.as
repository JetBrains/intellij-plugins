package com.intellij.flex.uiDesigner.gef.core {
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;

public interface IEditPartViewer {
  function get rootFigure():DisplayObjectContainer;
  function get control():DisplayObject;

  function get editDomain():EditDomain;

  /**
   * Sets the <code>{@link EditDomain}</code> for this viewer. The Viewer will route all mouse and
   * keyboard events to the {@link EditDomain}.
   */
  function set editDomain(value:EditDomain):void;
}
}

package com.intellij.flex.uiDesigner.gef.graphical {
import com.intellij.flex.uiDesigner.gef.core.EditPart;
import com.intellij.flex.uiDesigner.gef.core.IEditPartViewer;

import flash.display.DisplayObjectContainer;

internal class RootEditPart extends GraphicalEditPart {
  private var viewer:IEditPartViewer;
  private var rootFigure:DisplayObjectContainer;
  private var contentEditPart:EditPart;

  public function RootEditPart(viewer:IEditPartViewer, rootFigure:DisplayObjectContainer) {
    this.viewer = viewer;
    this.rootFigure = rootFigure;
    createLayers();
  }

  private function createLayers():void {

  }
}
}

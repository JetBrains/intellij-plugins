package com.intellij.flex.uiDesigner.gef.graphical {
import com.intellij.flex.uiDesigner.gef.core.AbstractEditPartViewer;
import com.intellij.flex.uiDesigner.gef.core.EditDomain;
import com.intellij.flex.uiDesigner.gef.core.IEditPartViewer;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Sprite;

public class GraphicalViewer extends AbstractEditPartViewer implements IEditPartViewer {
  protected var canvas:Sprite;
  private var rootEditPart:RootEditPart;

  public function GraphicalViewer(parent:Sprite) {
    canvas = new Sprite();
    parent.addChild(canvas);

    rootEditPart = new RootEditPart(this, rootFigure);
    rootEditPart.activate();
  }

  override public function get rootFigure():DisplayObjectContainer {
    return canvas;
  }

  override public function get control():DisplayObject {
    return canvas;
  }

  override public function set editDomain(value:EditDomain):void {
    super.editDomain = value;
    //eventManager = new EditEventManager(m_canvas, domain, this);
    //getRootFigureInternal().setEventManager(m_eventManager);
  }
}
}

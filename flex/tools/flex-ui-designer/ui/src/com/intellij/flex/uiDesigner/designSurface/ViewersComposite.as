package com.intellij.flex.uiDesigner.designSurface {
import cocoa.ControlView;

import com.intellij.flex.uiDesigner.gef.graphical.GraphicalViewer;
import com.intellij.flex.uiDesigner.gef.graphical.HeaderGraphicalViewer;
import com.intellij.flex.uiDesigner.gef.header.HeadersContainerEditPart;

import flash.display.Graphics;

public class ViewersComposite extends ControlView {
  private static const HEADER_SIZE:int = 15;

  private var horizontalViewer:HeaderGraphicalViewer;
  private var verticalViewer:HeaderGraphicalViewer;

  private var headersContainerHorizontal:HeadersContainerEditPart;
  private var headersContainerVertical:HeadersContainerEditPart;

  public function ViewersComposite() {
    horizontalViewer = new HeaderGraphicalViewer(this, true);
    verticalViewer = new HeaderGraphicalViewer(this, false);
    _viewer = new GraphicalViewer(this);

    // todo HeadersContextMenuProvider
    //horizontalViewer.setContextMenu(new HeadersContextMenuProvider(horizontalViewer));
    //verticalViewer.setContextMenu(new HeadersContextMenuProvider(verticalViewer));
  }

  private var _viewer:GraphicalViewer;
  public function get viewer():GraphicalViewer {
    return _viewer;
  }

  public function bindViewers():void {
    // todo bindViewers
    //HeadersEditPartFactory editPartFactory = new HeadersEditPartFactory();
    horizontalViewer.mainViewer = viewer;

    verticalViewer.mainViewer = viewer;
  }

  override protected function draw(w:int, h:int):void {
    var g:Graphics = graphics;
    g.clear();

    g.beginFill(0xffffff);
    
    g.lineStyle(1, 0x9f9f9f);
    g.moveTo(HEADER_SIZE, h);
    g.lineTo(HEADER_SIZE, HEADER_SIZE);
    g.lineTo(w, HEADER_SIZE);

    g.lineStyle();
    g.lineTo(w, h);
    g.lineTo(HEADER_SIZE, h);

    g.endFill();

    const x:int = HEADER_SIZE + 1;
    const y:int = HEADER_SIZE + 1;
    
    horizontalViewer.control.x = x;
    verticalViewer.control.y = y;
    _viewer.control.x = x;
    _viewer.control.y = y;
  }
}
}

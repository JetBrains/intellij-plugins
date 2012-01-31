package com.intellij.flex.uiDesigner.gef.graphical {
import flash.display.Sprite;

public class HeaderGraphicalViewer extends GraphicalViewer {
  private var horizontal:Boolean;

  public function HeaderGraphicalViewer(parent:Sprite, horizontal:Boolean) {
    this.horizontal = horizontal;

    super(parent);
  }

  private var _mainViewer:GraphicalViewer;
  public function set mainViewer(value:GraphicalViewer):void {
    _mainViewer = value;
    editDomain = _mainViewer.editDomain;

    //if (horizontal) {
    //
    //}
  }
}
}
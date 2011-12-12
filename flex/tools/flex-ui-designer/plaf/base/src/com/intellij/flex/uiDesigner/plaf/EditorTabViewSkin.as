package com.intellij.flex.uiDesigner.plaf {
import cocoa.Insets;
import cocoa.ListViewModifiableDataSource;
import cocoa.plaf.basic.AbstractTabViewSkin;
import cocoa.tabView.TabView;

import flash.display.Graphics;

import spark.primitives.Graphic;

internal class EditorTabViewSkin extends AbstractTabViewSkin {
  private static const CONTENT_INSETS:Insets = new Insets(0, 27 /* tab title bar */, 1, 0);
  
  override public function get contentInsets():Insets {
    return CONTENT_INSETS;
  }
  
  //override protected function createChildren():void {
  //  super.createChildren();
    
    //_borderShape.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
    //_borderShape.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);

    //addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
  //}

  //private function keyDownHandler(event:KeyboardEvent):void {
  //  if (event.keyCode == Keyboard.W && event.controlKey) {
  //    closeTab(itemIndex);
    //}
  //}

  override protected function draw(w:int, h:int):void {
    super.draw(w, h);

    const selectedIndex:int = tabBar.selectedIndex;
    if (selectedIndex != -1) {
      EditorTabBarRendererManager(tabBar.rendererManager).tabViewSizeChanged(selectedIndex, w, h);
    }
  }

  internal function closeTab(itemIndex:int):void {
    ListViewModifiableDataSource(TabView(component).dataSource).removeItemAt(itemIndex);
  }

  internal function getSelectedIndex():int {
    return tabBar.selectedIndex;
  }

  override public function hide():void {
    super.hide();
    EditorTabBarRendererManager(tabBar.rendererManager).clearSelected();
  }
}
}
package com.intellij.flex.uiDesigner.plaf {
import cocoa.Insets;
import cocoa.ListViewMutableDataSource;
import cocoa.Toolbar;
import cocoa.plaf.basic.AbstractTabViewSkin;
import cocoa.tabView.TabView;

internal class EditorTabViewSkin extends AbstractTabViewSkin {
  private static const CONTENT_INSETS:Insets = new Insets(0, 26 /* tab title bar */, 0, 0);
  
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

  internal function closeTab(itemIndex:int):void {
    ListViewMutableDataSource(TabView(component).dataSource).removeItemAt(itemIndex);
  }

  internal function getSelectedIndex():int {
    return tabBar.selectedIndex;
  }

  override protected function getToolbarHeight(toolbar:Toolbar):int {
    return 20;
  }

  override public function hide():void {
    super.hide();
    EditorTabBarRendererManager(tabBar.rendererManager).clearSelected();
  }
}
}
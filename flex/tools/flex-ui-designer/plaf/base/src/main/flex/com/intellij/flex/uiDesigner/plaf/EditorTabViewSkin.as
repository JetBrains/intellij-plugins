package com.intellij.flex.uiDesigner.plaf {
import cocoa.Insets;
import cocoa.ListViewModifiableDataSource;
import cocoa.plaf.basic.AbstractTabViewSkin;
import cocoa.tabView.TabView;

internal class EditorTabViewSkin extends AbstractTabViewSkin {
  private static const CONTENT_INSETS:Insets = new Insets(1, 25 /* tab title bar */ + 25 /* editor toolbar (always exists in our case — states bar for example) */, 1, 1);
  
  override public function get contentInsets():Insets {
    return CONTENT_INSETS;
  }
  
  override protected function createChildren():void {
    super.createChildren();
    
    //_borderShape.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
    //_borderShape.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);

    //addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
  }

  //private function keyDownHandler(event:KeyboardEvent):void {
  //  if (event.keyCode == Keyboard.W && event.controlKey) {
  //    closeTab(itemIndex);
    //}
  //}
  
  override protected function measure():void {
    // skip
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    super.updateDisplayList(w, h);

    const selectedIndex:int = tabBar.selectedIndex;
    if (selectedIndex != -1) {
      EditorTabBarRendererManager(tabBar.rendererManager).tabViewSizeChanged(selectedIndex, w, h);
    }
  }

  public function closeTab(itemIndex:int):void {
    ListViewModifiableDataSource(TabView(component).dataSource).removeItemAt(itemIndex);
  }

  override public function hide():void {
    super.hide();
    EditorTabBarRendererManager(tabBar.rendererManager).clearSelected();
  }
}
}
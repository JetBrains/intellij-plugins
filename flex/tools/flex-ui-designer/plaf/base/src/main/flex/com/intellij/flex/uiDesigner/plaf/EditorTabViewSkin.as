package com.intellij.flex.uiDesigner.plaf {
import cocoa.Insets;
import cocoa.PushButton;
import cocoa.plaf.basic.AbstractTabViewSkin;

import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.text.engine.TextLine;

import mx.core.IInvalidating;
import mx.core.IUIComponent;

internal class EditorTabViewSkin extends AbstractTabViewSkin {
  private static const CONTENT_INSETS:Insets = new Insets(1, 25 /* tab title bar */ + 25 /* editor toolbar (always exists in our case — states bar for example) */, 1, 1);
  
  private var closeButton:PushButton;
  private var closeButtonRelatedRenderer:EditorTabLabelRenderer;
  
  private var _borderShape:Sprite;
  public function get borderShape():Sprite {
    return _borderShape;
  }
  
  override public function get contentInsets():Insets {
    return CONTENT_INSETS;
  }
  
  override protected function createChildren():void {
    super.createChildren();
    
    assert(_borderShape == null);
    _borderShape = new Sprite();
    _borderShape.mouseChildren = false;
    addDisplayObject(_borderShape);
    
    segmentedControl.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
    segmentedControl.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
    
    _borderShape.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
    _borderShape.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
  }
  
  override protected function measure():void {
    // skip
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    super.updateDisplayList(w, h);

    const selectedIndex:int = segmentedControl.selectedIndex;
    if (selectedIndex != -1) {
      IInvalidating(segmentedControl.getElementAt(selectedIndex)).invalidateDisplayList();
    }
  }

  private function mouseOverHandler(event:MouseEvent):void {
    if (segmentedControl.dataProvider.length == 0) {
      return;
    }
    
    var closeButtonSkin:IUIComponent;
    if (event.target is EditorTabLabelRenderer) {
      closeButtonRelatedRenderer = EditorTabLabelRenderer(event.target);
    }
    else if (event.target == _borderShape) {
      closeButtonRelatedRenderer = EditorTabLabelRenderer(segmentedControl.getElementAt(segmentedControl.selectedIndex));
      if (event.localX < closeButtonRelatedRenderer.x || event.localX > (closeButtonRelatedRenderer.x + closeButtonRelatedRenderer.getExplicitOrMeasuredWidth()) ||
          (event.localY + 2 /* selected tab label has top offset -2 (height 22 instead of 20 for unseleted tab label) */) < closeButtonRelatedRenderer.y ||
          (event.localY - 4 /* 1 top stripe border stroke + 2 white background stripe + 1 bottom stripe border stroke */) >
          (closeButtonRelatedRenderer.y + closeButtonRelatedRenderer.getExplicitOrMeasuredHeight())) {
        return;
      }
    }
    
    if (closeButtonRelatedRenderer == null) {
      return;
    }
    
    if (closeButton == null) {
      closeButton = new PushButton();
      //closeButton.hoverable = true;
      closeButton.action = closeTab;
      closeButton.lafSubkey = "TabLabel";
      closeButtonSkin = closeButton.createView(laf);
      
      var asSprite:Sprite = Sprite(closeButtonSkin);
      addChild(asSprite);
      closeButton.skin.validateNow();
      closeButtonSkin.setActualSize(closeButtonSkin.getExplicitOrMeasuredWidth(), closeButtonSkin.getExplicitOrMeasuredHeight());
    }
    else {
      closeButtonSkin = IUIComponent(closeButton.skin);
      closeButtonSkin.visible = true;
    }

    var tabX:Number = closeButtonRelatedRenderer.x + closeButtonRelatedRenderer.getExplicitOrMeasuredWidth();
    closeButtonSkin.x = tabX - 2 - 1 - closeButtonSkin.getExplicitOrMeasuredWidth();
    
    if (closeButtonSkin.hitTestPoint(event.stageX, event.stageY)) {
      //ButtonSkinInteraction(closeButton.skin)
    }
  }
  
  private function closeTab():void {
    if (closeButtonRelatedRenderer.selected) {
      if (segmentedControl.dataProvider.length == 1) {
        closeButtonRelatedRenderer.clearSelected(this);
      }
      closeButtonRelatedRenderer.clearSelectedLabel();
    }
    
    segmentedControl.dataProvider.removeItemAt(closeButtonRelatedRenderer.itemIndex);
    
    hideCloseButton();
  }

  private function mouseOutHandler(event:MouseEvent):void {
    if (closeButton == null || event.relatedObject is TextLine) {
      return;
    }

    if ((event.target == _borderShape || event.target is EditorTabLabelRenderer) && event.relatedObject == closeButton.skin) {
      return;
    }

    hideCloseButton();
  }

  private function hideCloseButton():void {
    IUIComponent(closeButton.skin).visible = false;
    //closeButton.selected = false;
    closeButtonRelatedRenderer = null;
  }

  override public function hide():void {
    super.hide();
    _borderShape.graphics.clear();
  }
}
}
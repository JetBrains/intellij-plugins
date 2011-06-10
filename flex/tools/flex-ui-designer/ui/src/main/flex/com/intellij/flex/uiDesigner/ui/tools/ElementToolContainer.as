package com.intellij.flex.uiDesigner.ui.tools {
import cocoa.Insets;
import cocoa.LayoutlessContainer;

import flash.display.Sprite;
import flash.events.Event;
import flash.geom.Point;

import mx.core.ILayoutElement;

import org.flyti.plexus.Injectable;

public class ElementToolContainer extends LayoutlessContainer implements Injectable {
  private static var sharedPoint:Point = new Point();
  
  private var insets:Insets;
  private var element:Sprite;
  
  private var oldX:Number;
  private var oldY:Number;

  public function ElementToolContainer() {
    includeInLayout = false;
    mouseEnabled = false;
  }
  
  private var _elementLayoutChangeListeners:Vector.<ElementLayoutChangeListener>;
  public function set elementLayoutChangeListeners(value:Vector.<ElementLayoutChangeListener>):void {
    _elementLayoutChangeListeners = value;
  }

  public function setOffset(insets:Insets):void {
    this.insets = insets;
  }

  public function attach(untypedElement:Object):void {
    this.element = Sprite(untypedElement);
    element.addEventListener(Event.RENDER, renderHandler);
    
    moveHandler();

    var w:Number = untypedElement.getExplicitOrMeasuredWidth();
    var h:Number = untypedElement.getExplicitOrMeasuredHeight();
    updateSize(w, h);
  }

  private function renderHandler(event:Event):void {
    sharedPoint.x = element.x;
    sharedPoint.y = element.y;
    
    if (oldX != sharedPoint.x || oldY != sharedPoint.y) {
      moveHandler();
    }
    
    var o:Object = element;
    var w:Number = o.getExplicitOrMeasuredWidth();
    var h:Number = o.getExplicitOrMeasuredHeight();
    if (width != w || height != h) {
      updateSize(w, h);
    }
  }
  
  public function detach():void {
    element.removeEventListener(Event.RENDER, renderHandler);
    element = null;
  }

  private function moveHandler():void {
    sharedPoint.x = element.x;
    sharedPoint.y = element.y;
    
    oldX = sharedPoint.x;
    oldY = sharedPoint.y;
   
    sharedPoint = element.parent.localToGlobal(sharedPoint);
    sharedPoint = parent.globalToLocal(sharedPoint);
    
    x = sharedPoint.x;
    y = sharedPoint.y;
  }
  
  private function updateSize(w:Number, h:Number):void {
    setActualSize(w, h);
    for each (var elementLayoutChangeListener:ElementLayoutChangeListener in _elementLayoutChangeListeners) {
      elementLayoutChangeListener.sizeHandler(w, h);
    }
  }

  override protected function updateDisplayList(w:Number, h:Number):void {
    if (element != null) {
      var n:int = numChildren;
      while (n-- > 0) {
        var o:ILayoutElement = getChildAt(n) as ILayoutElement;
        if (o != null) {
          o.setLayoutBoundsSize(w, h);
        }
      }
    }
  }
}
}

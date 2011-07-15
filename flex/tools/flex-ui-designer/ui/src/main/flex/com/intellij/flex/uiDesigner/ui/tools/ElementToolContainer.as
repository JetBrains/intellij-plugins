package com.intellij.flex.uiDesigner.ui.tools {
import cocoa.LayoutlessContainer;

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.flex.ElementUtil;

import flash.events.Event;
import flash.geom.Point;

import mx.core.ILayoutElement;

import org.flyti.plexus.Injectable;

public class ElementToolContainer extends LayoutlessContainer implements Injectable {
  private static var sharedPoint:Point = new Point();

  private var element:Object;

  public function ElementToolContainer() {
    includeInLayout = false;
    mouseEnabled = false;
  }

  private var elementUtil:ElementUtil;
  //noinspection JSUnusedGlobalSymbols
  public function set elementDocument(value:Document):void {
    elementUtil = value == null ? null : value.systemManager.elementUtil;
  }
  
  private var _elementLayoutChangeListeners:Vector.<ElementLayoutChangeListener>;
  public function set elementLayoutChangeListeners(value:Vector.<ElementLayoutChangeListener>):void {
    _elementLayoutChangeListeners = value;
  }

  //public function setOffset(insets:Insets):void {
  //  this.insets = insets;
  //}

  public function attach(untypedElement:Object):void {
    this.element = untypedElement;
    addEventListener(Event.RENDER, renderHandler);

    getPosition();
    updatePosition();

    elementUtil.getSize(element, sharedPoint);
    updateSize(sharedPoint.x, sharedPoint.y);
  }

  private function renderHandler(event:Event):void {
    getPosition();
    if (x != sharedPoint.x) {
      x = sharedPoint.x;
    }
    if (y != sharedPoint.y) {
      y = sharedPoint.y;
    }

    elementUtil.getSize(element, sharedPoint);
    if (width != sharedPoint.x || height != sharedPoint.y) {
      updateSize(sharedPoint.x, sharedPoint.y);
    }
  }
  
  public function detach():void {
    removeEventListener(Event.RENDER, renderHandler);
    element = null;
  }

  private function getPosition():void {
    sharedPoint = parent.globalToLocal(elementUtil.getPosition(element, sharedPoint));
  }

  private function updatePosition():void {
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

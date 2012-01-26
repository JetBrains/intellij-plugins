package com.intellij.flex.uiDesigner.designSurface {
import cocoa.SpriteBackedView;

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.ComponentInfoProvider;

import flash.events.Event;
import flash.geom.Point;

import org.flyti.plexus.Injectable;

public class ElementToolContainer extends SpriteBackedView implements Injectable {
  private static var sharedPoint:Point = new Point();

  private var element:Object;

  public function ElementToolContainer() {
    mouseEnabled = false;
  }

  private var elementUtil:ComponentInfoProvider;
  private var layoutManager:Object;

  //noinspection JSUnusedGlobalSymbols
  public function set elementDocument(value:Document):void {
    if (value == null) {
      elementUtil = null;
      layoutManager = null;
    }
    else {
      elementUtil = value.displayManager.componentInfoProvider;
      layoutManager = value.displayManager.flexLayoutManager;
    }
  }
  
  private var _elementLayoutChangeListeners:Vector.<ElementLayoutChangeListener>;
  public function set elementLayoutChangeListeners(value:Vector.<ElementLayoutChangeListener>):void {
    _elementLayoutChangeListeners = value;
  }

  public function attach(untypedElement:Object):void {
    this.element = untypedElement;
    // IDEA-72373
    layoutManager.phasedInstantiationDone = renderHandler;
    // if layoutManager will be not participated in element validation
    addEventListener(Event.RENDER, renderHandler);

    getPosition();
    updatePosition();

    elementUtil.getSize(element, sharedPoint);
    updateSize(sharedPoint.x, sharedPoint.y);
  }

  private function renderHandler(event:Event = null):void {
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
    layoutManager.phasedInstantiationDone = null;
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
    for each (var elementLayoutChangeListener:ElementLayoutChangeListener in _elementLayoutChangeListeners) {
      elementLayoutChangeListener.sizeHandler(w, h);
    }
  }

  //override protected function draw(w:Number, h:Number):void {
  //  if (element != null) {
  //    var n:int = numChildren;
  //    while (n-- > 0) {
  //      var o:ILayoutElement = getChildAt(n) as ILayoutElement;
  //      if (o != null) {
  //        o.setLayoutBoundsSize(w, h);
  //      }
  //    }
  //  }
  //}
}
}

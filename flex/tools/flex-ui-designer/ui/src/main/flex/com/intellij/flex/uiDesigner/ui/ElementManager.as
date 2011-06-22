package com.intellij.flex.uiDesigner.ui {
import com.intellij.flex.uiDesigner.Document;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.MouseEvent;

import org.flyti.plexus.Injectable;

public class ElementManager extends EventDispatcher implements Injectable {
  private var skinClass:Class;
  private var iUIComponentClass:Class;
  private var skinnableContainerClass:Class;

  private var _element:Object;
  [Bindable(event="elementChanged")]
  public function get element():Object {
    return _element;
  }

  public function set element(value:Object):void {
    if (value != element) {
      _element = value;
      dispatchEvent(new Event("elementChanged"));
    }
  }

  private var _document:Document;
  public function set document(value:Document):void {
    if (value == _document) {
      return;
    }

    if (_document != null) {
      _document.systemManager.removeRealEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
    }

    _document = value;
    if (_document == null) {
      skinClass = null;
      iUIComponentClass = null;
      skinnableContainerClass = null;
    }
    else {
      _document.systemManager.addRealEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);

      skinClass = _document.module.getClass("spark.components.supportClasses.Skin");
      iUIComponentClass = _document.module.getClass("mx.core.IUIComponent");
      skinnableContainerClass = _document.module.context.getClassIfExists("spark.components.SkinnableContainer");
    }
    
    element = null;
  }

  private function mouseDownHandler(event:MouseEvent):void {
    var object:Object = findComponent(event.target);
    if (_element != object) {
      element = object;
    }
  }

  private function findComponent(object:Object):Object {
    while (object != null && (object is skinClass || !(object is iUIComponentClass))) {
      object = object.parent;
    }

    if (object == null) {
      return null;
    }

    var document:Object;
    while ((document = object.document) is skinClass &&
            (skinnableContainerClass == null || !("hostComponent" in document) || !(document.hostComponent is skinnableContainerClass))) {
      object = document.parent;
    }

    //if (object != null && object.parent is skinClass) {
    //  object = object.parent.parent;
    //}

    return object;
  }

  public function isSkin(element:Object):Boolean {
    return element is skinClass;
  }
}
}
package com.intellij.flex.uiDesigner {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.MouseEvent;

import org.flyti.plexus.Injectable;

public class ElementManager extends EventDispatcher implements Injectable {
  private var simpleStyleClientClass:Class;
  private var skinClass:Class;

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
      _document.systemManager.removeEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
    }

    _document = value;
    if (_document == null) {
      simpleStyleClientClass = null;
    }
    else {
      _document.systemManager.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
      simpleStyleClientClass = _document.module.getClass("mx.styles.IStyleClient");
      skinClass = _document.module.getClass("spark.components.supportClasses.Skin");
    }
    
    element = null;
  }

  private function mouseDownHandler(event:MouseEvent):void {
    var object:Object = event.target;
    while (object != null && (object is skinClass || !(object is simpleStyleClientClass))) {
      object = object.parent;
    }

    if (object != null && object.parent is skinClass) {
      object = object.parent.parent;
    }

    if (_element != object) {
      element = object;
    }
  }
}
}

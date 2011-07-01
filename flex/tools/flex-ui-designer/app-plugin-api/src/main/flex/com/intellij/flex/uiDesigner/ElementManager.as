package com.intellij.flex.uiDesigner {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

import org.flyti.plexus.Injectable;

public class ElementManager extends EventDispatcher implements Injectable {
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
      _document.systemManager.removeRealEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
      _document.systemManager.removeRealEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
    }

    _document = value;
    if (_document == null) {
      skinClass = null;
    }
    else {
      _document.systemManager.addRealEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
      _document.systemManager.addRealEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);

      skinClass = _document.module.getClass("spark.components.supportClasses.Skin");
    }

    element = null;
  }

  private function keyDownHandler(event:KeyboardEvent):void {
    if (_element != null && event.keyCode == Keyboard.F4 && !event.altKey && !event.ctrlKey && !event.shiftKey) {
      DocumentFactoryManager(_document.module.project.getComponent(DocumentFactoryManager)).jumpToObjectDeclaration(_element, _document);
    }
  }

  private function mouseDownHandler(event:MouseEvent):void {
    var object:Object = findComponent(event);
    if (_element != object) {
      //if (object is InteractiveObject) {
      //  InteractiveObject(object).stage.focus = InteractiveObject(object);
      //  trace(InteractiveObject(object).stage.focus, object);
      //}
      element = object;
    }
  }

  private function findComponent(event:MouseEvent):Object {
    return _document.module.getClass("com.intellij.flex.uiDesigner.flex.ElementUtil")["getObjectUnderPoint"](_document.systemManager.stage, event.stageX, event.stageY);
  }

  public function isSkin(element:Object):Boolean {
    return element is skinClass;
  }
}
} 
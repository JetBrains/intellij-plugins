package com.intellij.flex.uiDesigner {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

import org.flyti.plexus.Injectable;

public class ElementManager extends EventDispatcher implements Injectable {
  private var _element:Object;
  [Bindable(event="elementChanged")]
  public function get element():Object {
    return _element;
  }

  public function set element(value:Object):void {
    if (value != element) {
      if (_element != null) {
        _document.displayManager.elementUtil.getDisplayObject(_element).removeEventListener(Event.REMOVED_FROM_STAGE, removedFromStageHandler);
      }
      
      _element = value;
      
      if (_element != null) {
        _document.displayManager.elementUtil.getDisplayObject(_element).addEventListener(Event.REMOVED_FROM_STAGE, removedFromStageHandler);
      }
      
      dispatchEvent(new Event("elementChanged"));
    }
  }

  private var _document:Document;
  public function set document(value:Document):void {
    if (value == _document) {
      return;
    }

    if (_document != null) {
      _document.displayManager.removeRealEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
      _document.displayManager.removeRealEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
    }

    _document = value;

    if (_document != null) {
      _document.displayManager.addRealEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
      _document.displayManager.addRealEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
    }
    else {
      element = null;
    }
  }

  private function keyDownHandler(event:KeyboardEvent):void {
    if (_element != null && event.keyCode == Keyboard.F4 && !event.altKey && !event.ctrlKey && !event.shiftKey) {
      DocumentFactoryManager(_document.module.project.getComponent(DocumentFactoryManager)).jumpToObjectDeclaration(_element, _document);
    }
  }

  private function mouseDownHandler(event:MouseEvent):void {
    var object:Object = findComponent(event);
    if (object != _element) {
      element = object;
    }
  }

  private function findComponent(event:MouseEvent):Object {
    return _document.displayManager.elementUtil.getObjectUnderPoint(_document.displayManager.stage, event.stageX, event.stageY);
  }

  public function fillBreadcrumbs(element:Object, source:Vector.<String>):int {
    return _document.displayManager.elementUtil.fillBreadcrumbs(element, source);
  }

  private function removedFromStageHandler(event:Event):void {
    if (_element == event.currentTarget) {
      element = null;
    }
  }
}
} 
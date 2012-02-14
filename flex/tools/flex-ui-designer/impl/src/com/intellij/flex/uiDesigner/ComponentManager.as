package com.intellij.flex.uiDesigner {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

import org.flyti.plexus.Injectable;

public class ComponentManager extends EventDispatcher implements Injectable {
  private var _component:Object;
  [Bindable(event="componentChanged")]
  public function get component():Object {
    return _component;
  }

  public function set component(value:Object):void {
    if (value != component) {
      if (_component != null) {
        _document.displayManager.componentInfoProvider.getDisplayObject(_component).removeEventListener(Event.REMOVED_FROM_STAGE, removedFromStageHandler);
      }
      
      _component = value;
      
      if (_component != null) {
        _document.displayManager.componentInfoProvider.getDisplayObject(_component).addEventListener(Event.REMOVED_FROM_STAGE, removedFromStageHandler);
      }
      
      dispatchEvent(new Event("componentChanged"));
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
      component = null;
    }
  }

  private var _autoScrollToSource:Boolean;
  public function set autoScrollToSource(value:Boolean):void {
    _autoScrollToSource = value;
  }

  private function keyDownHandler(event:KeyboardEvent):void {
    if (_component != null && event.keyCode == Keyboard.F4 && !event.altKey && !event.ctrlKey && !event.shiftKey) {
      DocumentFactoryManager(_document.module.project.getComponent(DocumentFactoryManager)).jumpToObjectDeclaration(_component, _document, true);
    }
  }

  private function mouseDownHandler(event:MouseEvent):void {
    var object:Object = findComponent(event);
    if (object != _component) {
      component = object;
      if (object != null && _autoScrollToSource) {
        DocumentFactoryManager(_document.module.project.getComponent(DocumentFactoryManager)).jumpToObjectDeclaration(_component, _document, false);
      }
    }
  }

  private function findComponent(event:MouseEvent):Object {
    return _document.displayManager.componentInfoProvider.getComponentUnderPoint(_document.displayManager.realStage, event.stageX, event.stageY);
  }

  public function fillBreadcrumbs(element:Object, source:Vector.<String>):int {
    return _document.displayManager.componentInfoProvider.fillBreadcrumbs(element, source);
  }

  private function removedFromStageHandler(event:Event):void {
    if (_component == event.currentTarget) {
      component = null;
    }
  }
}
} 
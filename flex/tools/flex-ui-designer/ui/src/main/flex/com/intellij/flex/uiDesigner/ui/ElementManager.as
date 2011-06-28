package com.intellij.flex.uiDesigner.ui {
import cocoa.Component;

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.DocumentFactoryManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.InteractiveObject;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

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

  public function registerKeyboardEventHandler(eventDispatcher:IEventDispatcher):void {
    if (eventDispatcher is Component) {
      eventDispatcher = Component(eventDispatcher).skin;
    }
    
    eventDispatcher.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
  }

  private function keyDownHandler(event:KeyboardEvent):void {
    if (_element != null && event.keyCode == Keyboard.F4 && !event.altKey && !event.ctrlKey && !event.shiftKey &&
        (event.target == _element ||
         (_element is DisplayObjectContainer && DisplayObjectContainer(_element).contains(DisplayObject(event.target))))) {
      DocumentFactoryManager(_document.module.project.getComponent(DocumentFactoryManager)).jumpToObjectDeclaration(_element, _document);
    }
  }

  private function mouseDownHandler(event:MouseEvent):void {
    var object:Object = findComponent(event.target);
    if (_element != object) {
      if (object is InteractiveObject) {
        InteractiveObject(object).stage.focus = InteractiveObject(object);
        trace(InteractiveObject(object).stage.focus, object);
      }
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
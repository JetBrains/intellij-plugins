package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Stage;

import mx.core.UIComponent;
import mx.events.ResizeEvent;
import mx.managers.ISystemManager;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IAdvancedStyleClient;

flex::gt_4_1
import mx.utils.DensityUtil;

internal final class FakeTopLevelApplication extends UIComponent {
  private var manager:FlexDocumentDisplayManager;
  private var uiComponent:UIComponent;

  private var emptyParameters:Object;

  public function FakeTopLevelApplication(manager:FlexDocumentDisplayManager, moduleFactory:FlexModuleFactory) {
    this.manager = manager;
    this.moduleFactory = moduleFactory;
  }

  internal function setUIComponent(value:UIComponent):void {
    uiComponent = value;
  }

  override public function get parent():DisplayObjectContainer {
    return manager;
  }

  override public function get root():DisplayObject {
    return manager;
  }

  override public function get systemManager():ISystemManager {
    return manager;
  }

  override public function get stage():Stage {
    return manager.stage;
  }

  //noinspection JSMethodCanBeStatic
  flex::gt_4_1
  public function get runtimeDPI():Number {
    return DensityUtil.getRuntimeDPI();
  }

  private var _applicationDPI:int = -1;
  //noinspection JSUnusedGlobalSymbols
  flex::gt_4_1
  public function get applicationDPI():Number {
    if (_applicationDPI == -1) {
      _applicationDPI = runtimeDPI;
    }

    return _applicationDPI;
  }

  //noinspection JSUnusedLocalSymbols,JSUnusedGlobalSymbols
  flex::gt_4_1
  public function set applicationDPI(value:Number):void {
  }

  //noinspection JSUnusedGlobalSymbols
  public function get parameters():Object {
    if (emptyParameters == null) {
      emptyParameters = {};
    }

    return emptyParameters;
  }

  flex::gt_4_1
  override public function addStyleClient(styleClient:IAdvancedStyleClient):void {
    uiComponent.addStyleClient(styleClient);
  }

  override public function getStyle(styleProp:String):* {
    return uiComponent.getStyle(styleProp);
  }

  override public function get inheritingStyles():Object {
    return uiComponent.inheritingStyles;
  }

  override public function get nonInheritingStyles():Object {
    return uiComponent.nonInheritingStyles;
  }

  override public function getClassStyleDeclarations():Array {
    return uiComponent.getClassStyleDeclarations();
  }

  override public function get styleName():Object {
    return uiComponent.styleName;
  }

  override public function get styleDeclaration():CSSStyleDeclaration {
    return uiComponent.styleDeclaration;
  }

  override public function get className():String {
    return uiComponent.className;
  }

  override public function notifyStyleChangeInChildren(styleProp:String, recursive:Boolean):void {
    uiComponent.notifyStyleChangeInChildren(styleProp, recursive);
  }

//noinspection JSUnusedGlobalSymbols
  public function get aspectRatio():String {
    return width > height ? "landscape" : "portrait";
  }

  override public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0,
                                            useWeakReference:Boolean = false):void {
    if (type == ResizeEvent.RESIZE) {
      uiComponent.addEventListener(type, listener, useCapture, priority, useWeakReference);
    }
    else {
      super.addEventListener(type, listener, useCapture, priority, useWeakReference);
    }
  }

  override public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
    if (type == ResizeEvent.RESIZE) {
      uiComponent.removeEventListener(type, listener, useCapture);
    }
    else {
      super.removeEventListener(type, listener, useCapture);
    }
  }
}
}

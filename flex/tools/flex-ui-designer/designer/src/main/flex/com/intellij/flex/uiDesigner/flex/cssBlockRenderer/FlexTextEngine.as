package com.intellij.flex.uiDesigner.flex.cssBlockRenderer {
import flash.events.Event;

import mx.managers.ILayoutManagerClient;
import mx.managers.LayoutManager;

import org.tinytlf.TextEngine;

public class FlexTextEngine extends TextEngine implements ILayoutManagerClient {
  private var scrollHandler:Function;
  private var invalid:Boolean;

  public function FlexTextEngine(scrollHandler:Function) {
    this.scrollHandler = scrollHandler;
  }

  private var _handValidation:Boolean;
  public function set handValidation(value:Boolean):void {
    if (value == _handValidation) {
      return; 
    }
    
    if (!value && invalid) {
      render();
      invalid = false;
    }
    
    _handValidation = value;
  }

  override protected function invalidateStage():void {
    if (_handValidation) {
      invalid = true;
    }
    else {
      LayoutManager.getInstance().invalidateDisplayList(this);
    }
  }

  public function get initialized():Boolean {
    return true;
  }

  public function set initialized(value:Boolean):void {
  }

  private var _nestLevel:int;
  public function get nestLevel():int {
    return _nestLevel;
  }

  public function set nestLevel(value:int):void {
    _nestLevel = value;
  }

  public function get processedDescriptors():Boolean {
    return true;
  }

  public function set processedDescriptors(value:Boolean):void {
  }

  public function get updateCompletePendingFlag():Boolean {
    return false;
  }

  public function set updateCompletePendingFlag(value:Boolean):void {
  }

  public function validateProperties():void {
  }

  public function validateSize(recursive:Boolean = false):void {
  }

  public function validateDisplayList():void {
    render();
  }

  public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
  }

  public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
  }

  public function dispatchEvent(event:Event):Boolean {
    return false;
  }

  public function hasEventListener(type:String):Boolean {
    return false;
  }

  public function willTrigger(type:String):Boolean {
    return false;
  }

  override public function set scrollPosition(value:Number):void {
    scrollHandler(value);
  }
}
}

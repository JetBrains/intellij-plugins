package com.intellij.flex.uiDesigner.flex {
import flash.events.Event;

import mx.managers.IBrowserManager;

public class BrowserManagerImpl implements IBrowserManager {
  public function get base():String {
    return "";
  }

  public function get fragment():String {
    return "";
  }

  public function get title():String {
    return "";
  }

  public function get url():String {
    return "";
  }

  public function setFragment(value:String):void {
  }

  public function setTitle(value:String):void {
  }

  public function init(value:String = null, title:String = null):void {
  }

  public function initForHistoryManager():void {
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
}
}

package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.display.LoaderInfo;
import flash.display.Sprite;
import flash.display.Stage;
import flash.events.Event;
import flash.geom.Point;
import flash.geom.Rectangle;
import flash.text.TextFormat;
import flash.utils.Dictionary;

import mx.core.IChildList;

flex::v4_5
import mx.core.RSLData;

import mx.managers.ISystemManager;

internal class TopLevelSystemManager implements ISystemManager {
  private var _stage:Stage;

  public function TopLevelSystemManager(stage:Stage) {
    _stage = stage;
  }

  public function get cursorChildren():IChildList {
    return null;
  }

  public function get document():Object {
    return null;
  }

  public function set document(value:Object):void {
  }

  public function get embeddedFontList():Object {
    return null;
  }

  public function get focusPane():Sprite {
    return null;
  }

  public function set focusPane(value:Sprite):void {
  }

  public function get isProxy():Boolean {
    return false;
  }

  public function get loaderInfo():LoaderInfo {
    return null;
  }

  public function get numModalWindows():int {
    return 0;
  }

  public function set numModalWindows(value:int):void {
  }

  public function get popUpChildren():IChildList {
    return null;
  }

  public function get rawChildren():IChildList {
    return null;
  }

  public function get screen():Rectangle {
    return null;
  }

  public function get stage():Stage {
    return _stage;
  }

  public function get toolTipChildren():IChildList {
    return null;
  }

  public function get topLevelSystemManager():ISystemManager {
    return null;
  }

  public function getDefinitionByName(name:String):Object {
    return null;
  }

  public function isTopLevel():Boolean {
    return true;
  }

  public function isFontFaceEmbedded(tf:TextFormat):Boolean {
    return false;
  }

  public function isTopLevelRoot():Boolean {
    return false;
  }

  public function getTopLevelRoot():DisplayObject {
    return null;
  }

  public function getSandboxRoot():DisplayObject {
    return null;
  }

  public function deployMouseShields(deploy:Boolean):void {
  }

  public function invalidateParentSizeAndDisplayList():void {
  }

  public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0,
                                   useWeakReference:Boolean = false):void {
    //trace("tsm: skip addEventListener " + type);
  }

  public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
    trace("tsm: skip removeEventListener " + type);
  }

  public function dispatchEvent(event:Event):Boolean {
    trace("tsm: skip dispatchEvent " + event.toString());
    return false;
  }

  public function hasEventListener(type:String):Boolean {
    // Event.RENDER or Event.ENTER_FRAME are not problem — LayoutManager never use this method, in any case (twice add event lisneter), it is not cause memory leaks
    return type == "initializeError" || type == "callLaterError";
  }

  public function willTrigger(type:String):Boolean {
    return hasEventListener(type);
  }

  public function get numChildren():int {
    return 0;
  }

  public function addChild(child:DisplayObject):DisplayObject {
    return null;
  }

  public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    return null;
  }

  public function removeChild(child:DisplayObject):DisplayObject {
    return null;
  }

  public function removeChildAt(index:int):DisplayObject {
    return null;
  }

  public function getChildAt(index:int):DisplayObject {
    return null;
  }

  public function getChildByName(name:String):DisplayObject {
    return null;
  }

  public function getChildIndex(child:DisplayObject):int {
    return 0;
  }

  public function setChildIndex(child:DisplayObject, newIndex:int):void {
  }

  public function getObjectsUnderPoint(point:Point):Array {
    return null;
  }

  public function contains(child:DisplayObject):Boolean {
    return false;
  }

  public function get preloadedRSLs():Dictionary {
    return null;
  }

  public function allowDomain(... rest):void {
  }

  public function allowInsecureDomain(... rest):void {
  }

  public function callInContext(fn:Function, thisArg:Object, argArray:Array, returns:Boolean = true):* {
    return null;
  }

  public function create(... rest):Object {
    return null;
  }

  private var fakeSystemManagerChildManager:FakeSystemManagerChildManager;
  public function getImplementation(interfaceName:String):Object {
    if (interfaceName == SystemManager.SYSTEM_MANAGER_CHILD_MANAGER) {
      if (fakeSystemManagerChildManager == null) {
        fakeSystemManagerChildManager = new FakeSystemManagerChildManager();
      }
      return fakeSystemManagerChildManager;
    }
    else {
      trace("tsm: skip getImplementation: " + interfaceName);
      return null;
    }
  }

  public function info():Object {
    return null;
  }

  public function registerImplementation(interfaceName:String, impl:Object):void {
  }

  flex::v4_5
  public function getVisibleApplicationRect(bounds:Rectangle = null, skipToSandboxRoot:Boolean = false):Rectangle {
    return null;
  }

  flex::v4_1
  public function getVisibleApplicationRect(bounds:Rectangle = null):Rectangle {
    return null;
  }

  flex::v4_5
  public function addPreloadedRSL(loaderInfo:LoaderInfo, rsl:Vector.<RSLData>):void {
    throw new Error("forbidden");
  }
}
}

class FakeSystemManagerChildManager {
  // mx.managers::ISystemManagerChildManager, ChildManager, "cm.notifyStyleChangeInChildren(styleProp, true);" in CSSStyleDeclaration
  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function notifyStyleChangeInChildren(styleProp:String, recursive:Boolean):void {
  }
}

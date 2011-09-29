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
import mx.managers.ISystemManager;

// see mx/managers/DragManagerImpl.as sm = SystemManagerGlobals.topLevelSystemManagers[0];
internal class TopLevelSystemManagerProxy extends BaseFlexModuleFactoryImpl implements ISystemManager {
  internal var activeSystemManager:SystemManager;

  public function get cursorChildren():IChildList {
    return activeSystemManager.cursorChildren;
  }

  public function get document():Object {
    return activeSystemManager.document;
  }

  public function set document(value:Object):void {
    activeSystemManager.document = value;
  }

  public function get embeddedFontList():Object {
    return activeSystemManager.embeddedFontList;
  }

  public function get focusPane():Sprite {
    return activeSystemManager.focusPane;
  }

  public function set focusPane(value:Sprite):void {
    activeSystemManager.focusPane = value;
  }

  public function get isProxy():Boolean {
    return activeSystemManager.isProxy;
  }

  public function get loaderInfo():LoaderInfo {
    return activeSystemManager.loaderInfo;
  }

  public function get numModalWindows():int {
    return activeSystemManager.numModalWindows;
  }

  public function set numModalWindows(value:int):void {
    activeSystemManager.numModalWindows = value;
  }

  public function get popUpChildren():IChildList {
    return activeSystemManager.popUpChildren;
  }

  public function get rawChildren():IChildList {
    return activeSystemManager.rawChildren;
  }

  public function get screen():Rectangle {
    return activeSystemManager.screen;
  }

  public function get stage():Stage {
    return activeSystemManager.stage;
  }

  public function get toolTipChildren():IChildList {
    return activeSystemManager.toolTipChildren;
  }

  public function get topLevelSystemManager():ISystemManager {
    return activeSystemManager.topLevelSystemManager;
  }

  public function getDefinitionByName(name:String):Object {
    return activeSystemManager.getDefinitionByName(name);
  }

  public function isTopLevel():Boolean {
    return activeSystemManager.isTopLevel();
  }

  public function isFontFaceEmbedded(tf:TextFormat):Boolean {
    return activeSystemManager.isFontFaceEmbedded(tf);
  }

  public function isTopLevelRoot():Boolean {
    return activeSystemManager.isTopLevelRoot();
  }

  public function getTopLevelRoot():DisplayObject {
    return activeSystemManager.getTopLevelRoot();
  }

  public function getSandboxRoot():DisplayObject {
    return activeSystemManager.getSandboxRoot();
  }

  public function deployMouseShields(deploy:Boolean):void {
    activeSystemManager.deployMouseShields(deploy);
  }

  public function invalidateParentSizeAndDisplayList():void {
    activeSystemManager.invalidateParentSizeAndDisplayList();
  }

  public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0,
                                   useWeakReference:Boolean = false):void {
    activeSystemManager.addEventListener(type, listener, useCapture, priority, useWeakReference);
  }

  public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
    activeSystemManager.removeEventListener(type, listener, useCapture);
  }

  public function dispatchEvent(event:Event):Boolean {
    return activeSystemManager.dispatchEvent(event);
  }

  public function hasEventListener(type:String):Boolean {
    return activeSystemManager.hasEventListener(type);
  }

  public function willTrigger(type:String):Boolean {
    return activeSystemManager.willTrigger(type);
  }

  public function get numChildren():int {
    return activeSystemManager.numChildren;
  }

  public function addChild(child:DisplayObject):DisplayObject {
    return activeSystemManager.addChild(child);
  }

  public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    return activeSystemManager.addChildAt(child, index);
  }

  public function removeChild(child:DisplayObject):DisplayObject {
    return activeSystemManager.removeChild(child);
  }

  public function removeChildAt(index:int):DisplayObject {
    return activeSystemManager.removeChildAt(index);
  }

  public function getChildAt(index:int):DisplayObject {
    return activeSystemManager.getChildAt(index);
  }

  public function getChildByName(name:String):DisplayObject {
    return activeSystemManager.getChildByName(name);
  }

  public function getChildIndex(child:DisplayObject):int {
    return activeSystemManager.getChildIndex(child);
  }

  public function setChildIndex(child:DisplayObject, newIndex:int):void {
    activeSystemManager.setChildIndex(child, newIndex)
  }

  public function getObjectsUnderPoint(point:Point):Array {
    return activeSystemManager.getObjectsUnderPoint(point);
  }

  public function contains(child:DisplayObject):Boolean {
    return activeSystemManager.contains(child);
  }

  public function get preloadedRSLs():Dictionary {
    return activeSystemManager.preloadedRSLs;
  }

  public function allowDomain(... rest):void {
    activeSystemManager.allowDomain.apply(null, rest);
  }

  public function allowInsecureDomain(... rest):void {
    activeSystemManager.allowInsecureDomain.apply(null, rest);
  }

  public function callInContext(fn:Function, thisArg:Object, argArray:Array, returns:Boolean = true):* {
    return activeSystemManager.callInContext(fn, thisArg, argArray, returns);
  }

  public function create(... rest):Object {
    return activeSystemManager.create.apply(null, rest);
  }

  public function getImplementation(interfaceName:String):Object {
    return activeSystemManager.getImplementation(interfaceName);
  }

  public function info():Object {
    return activeSystemManager.info();
  }

  public function registerImplementation(interfaceName:String, impl:Object):void {
    activeSystemManager.registerImplementation(interfaceName, impl);
  }

  flex::v4_5
  public function getVisibleApplicationRect(bounds:Rectangle = null, skipToSandboxRoot:Boolean = false):Rectangle {
    return activeSystemManager.getVisibleApplicationRect(bounds, skipToSandboxRoot);
  }

  flex::v4_1
  public function getVisibleApplicationRect(bounds:Rectangle = null):Rectangle {
    return activeSystemManager.getVisibleApplicationRect(bounds);
  }
}
}

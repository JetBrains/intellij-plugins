package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;

import flash.display.DisplayObject;
import flash.display.Stage;
import flash.geom.Rectangle;

public interface DocumentDisplayManager {
  function setUserDocument(object:DisplayObject):void;

  function get preferredDocumentWidth():int;
  function get preferredDocumentHeight():int;

  function setActualDocumentSize(w:int, h:int):void;
  
  function getDefinitionByName(name:String):Object;

  function addRealEventListener(type:String, listener:Function, useCapture:Boolean = false):void;
  function removeRealEventListener(type:String, listener:Function):void;

  function get stage():Stage;

  function init(moduleFactory:Object, uiErrorHandler:UiErrorHandler,
                mainFocusManager:MainFocusManagerSB, documentFactory:Object):void;

  function removeEventHandlers():void;

  function added():void;

  function deactivated():void;

  function activated():void;

  function get sharedInitialized():Boolean;

  function initShared(stage:Stage, project:Object, resourceBundleProvider:ResourceBundleProvider,
                      uiErrorHandler:UiErrorHandler):void;

  function get elementUtil():ElementInfoProvider;

  function setStyleManagerForTalentAdobeEngineers(value:Boolean):void;

  function get documentFactory():Object;

  function get layoutManager():Object;
}
}

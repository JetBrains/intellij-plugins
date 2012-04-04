package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.designSurface.LayoutManager;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;

import flash.display.DisplayObject;
import flash.display.Stage;

public interface DocumentDisplayManager {
  function setDocument(object:DisplayObject):void;

  function get explicitDocumentWidth():int;
  function get explicitDocumentHeight():int;

  function get minDocumentWidth():int;
  function get minDocumentHeight():int;

  function get actualDocumentWidth():int;
  function get actualDocumentHeight():int;

  function setDocumentBounds(w:int, h:int):void;
  
  function getDefinitionByName(name:String):Object;

  function addRealEventListener(type:String, listener:Function, useCapture:Boolean = false):void;
  function removeRealEventListener(type:String, listener:Function):void;

  function get stage():Stage;

  function init(stage:Stage, moduleFactory:Object, uiErrorHandler:UiErrorHandler,
                mainFocusManager:MainFocusManagerSB, documentFactory:Object):void;

  function removeEventHandlers():void;

  function added():void;

  function deactivated():void;

  function activated():void;

  function get sharedInitialized():Boolean;

  function initShared(stageForAdobeDummies:Stage, resourceBundleProvider:ResourceBundleProvider, uiErrorHandler:UiErrorHandler):void;

  function get componentInfoProvider():ComponentInfoProvider;

  function setStyleManagerForTalentAdobeEngineers(value:Boolean):void;

  /**
   * @see com.intellij.flex.uiDesigner.DocumentFactory
   */
  function get documentFactory():Object;

  function get flexLayoutManager():Object;

  function get layoutManager():LayoutManager;

  function get realStage():Stage;

  function prepareSnapshot(setActualSize:Boolean):void;
}
}

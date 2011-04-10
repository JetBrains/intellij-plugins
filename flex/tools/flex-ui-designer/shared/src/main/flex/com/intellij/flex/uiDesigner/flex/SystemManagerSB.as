package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.display.Stage;
import flash.geom.Rectangle;

public interface SystemManagerSB {
  function setUserDocument(object:DisplayObject):void;

  function get explicitDocumentSize():Rectangle;
  function setActualDocumentSize(w:Number, h:Number):void;
  
  function getDefinitionByName(name:String):Object;

  function addRealEventListener(type:String, listener:Function):void;
  function removeRealEventListener(type:String, listener:Function):void;

  function get stage():Stage;

  function init(moduleFactory:Object, stage:Stage, uiInitializeOrCallLaterErrorHandler:Function):void;

  function removeProxyMouseEventHandlers():void;
}
}

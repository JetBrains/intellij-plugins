package com.intellij.flex.uiDesigner.flex {
import flash.display.DisplayObject;
import flash.events.IEventDispatcher;
import flash.geom.Rectangle;

public interface SystemManagerSB extends IEventDispatcher {
  function setUserDocument(object:DisplayObject):void;

  function get explicitDocumentSize():Rectangle;
  function setActualDocumentSize(w:Number, h:Number):void;
  
  function getDefinitionByName(name:String):Object;
}
}

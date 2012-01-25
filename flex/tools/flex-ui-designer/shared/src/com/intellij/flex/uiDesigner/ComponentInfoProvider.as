package com.intellij.flex.uiDesigner {
import flash.display.DisplayObject;
import flash.display.Stage;
import flash.geom.Point;

public interface ComponentInfoProvider {
  function getComponentUnderPoint(stage:Stage, stageX:Number, stageY:Number):Object;

  function fillBreadcrumbs(element:Object, source:Vector.<String>):int;

  function getDisplayObject(element:Object):DisplayObject;

  function getPosition(element:Object, result:Point):Point;

  function getSize(element:Object, result:Point):void;
}
}

package com.intellij.flex.uiDesigner.designSurface {
import flash.display.DisplayObjectContainer;
import flash.geom.Point;

public interface Tool {
  function attach(element:Object, toolContainer:ElementToolContainer):void;
  function detach():void;

  function activate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>):void;
  function deactivate():void;
}
}

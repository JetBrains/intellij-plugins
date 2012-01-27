package com.intellij.flex.uiDesigner.designSurface {
import flash.display.DisplayObjectContainer;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;

public interface Tool {
  function activate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>, dataContext:DataContext):void;
  function deactivate():void;
}
}

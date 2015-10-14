package com.intellij.flex.uiDesigner.designSurface {
import flash.display.DisplayObjectContainer;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;

public interface Tool {
  /**
     * Called when this tool becomes the active tool for the {@link EditDomain}. Implementors can
     * perform any necessary initialization here.
     */
  function activate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>, dataContext:DataContext):void;
  /**
     * Called when another Tool becomes the active tool for the {@link EditDomain}. Implementors can
     * perform state clean-up or to free resources.
     */
  function deactivate():void;



}
}

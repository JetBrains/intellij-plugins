package com.intellij.flex.uiDesigner.designSurface {
import flash.display.DisplayObjectContainer;
import flash.geom.Point;

import org.flyti.plexus.Injectable;
import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataManager;

public class ToolManager implements Injectable {
  private var tools:Vector.<Tool> = new <Tool>[new GridTool()];

  private var _areaLocations:Vector.<Point>;
  public function set areaLocations(value:Vector.<Point>):void {
    _areaLocations = value;
  }

  private var _displayObjectContainer:DisplayObjectContainer;
  public function set displayObjectContainer(displayContainer:DisplayObjectContainer):void {
    _displayObjectContainer = displayContainer;
  }

  //private var _toolContainer:ElementToolContainer;
  //public function set toolContainer(value:ElementToolContainer):void {
  //  assert(_toolContainer == null);
  //  _toolContainer = value;
  //
  //  var elementLayoutChangeListeners:Vector.<ElementLayoutChangeListener> = new Vector.<ElementLayoutChangeListener>();
  //  for each (var tool:Tool in tools) {
  //    if (tool is ElementLayoutChangeListener) {
  //      elementLayoutChangeListeners.push(tool);
  //    }
  //  }
  //
  //  elementLayoutChangeListeners.fixed = true;
  //  _toolContainer.elementLayoutChangeListeners = elementLayoutChangeListeners;
  //}

  private var _component:Object;
  public function set component(value:Object):void {
    if (value == _component) {
      return;
    }

    var wasActivated:Boolean = _component != null;
    if (wasActivated) {
      if (value == null) {
        deactivateTools();
      }
    }
    else if (value != null) {
      activateTools();
    }
    
    _component = value;
  }
  
  private function activateTools():void {
    var dataContext:DataContext = DataManager.instance.getDataContext(_displayObjectContainer);
    for each (var tool:Tool in tools) {
      tool.activate(_displayObjectContainer, _areaLocations, dataContext);
    }
    
    // after attach all tools — toolContainer call all elementLayoutChangeListeners 
    //_toolContainer.attach(_component);
  }

  private function deactivateTools():void {
    for each (var tool:Tool in tools) {
      tool.deactivate();
    }
  }
}
}
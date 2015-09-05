package com.intellij.flex.uiDesigner.designSurface {
import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.PlatformDataKeys;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.geom.Point;

import org.flyti.plexus.Injectable;
import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataManager;

public class ToolManager implements Injectable {
  private var tools:Vector.<Tool> = new <Tool>[new GridTool(), new DocumentCanvasResizer()];

  private var selectionTool:SelectionKnobsTool = new SelectionKnobsTool();
  private var elementToolContainer:ElementToolContainer = new ElementToolContainer();

  private var _areaLocations:Vector.<Point>;
  public function set areaLocations(value:Vector.<Point>):void {
    _areaLocations = value;
  }

  private var _displayObjectContainer:DisplayObjectContainer;
  public function set displayObjectContainer(displayContainer:DisplayObjectContainer):void {
    _displayObjectContainer = displayContainer;
  }

  private var _toolContainer:ElementToolContainer;
  public function set toolContainer(value:ElementToolContainer):void {
    assert(_toolContainer == null);
    _toolContainer = value;

    var elementLayoutChangeListeners:Vector.<ElementLayoutChangeListener> = new Vector.<ElementLayoutChangeListener>();
    for each (var tool:Tool in tools) {
      if (tool is ElementLayoutChangeListener) {
        elementLayoutChangeListeners.push(tool);
      }
    }

    elementLayoutChangeListeners.fixed = true;
    _toolContainer.elementLayoutChangeListeners = elementLayoutChangeListeners;
  }

  public function set document(value:Document):void {
    elementToolContainer.elementDocument = value;
  }

  private var _component:Object;
  public function set component(value:Object):void {
    if (value == _component) {
      return;
    }

    const wasActivated:Boolean = _component != null;
    _component = value;
    if (wasActivated) {
      if (value == null) {
        deactivateTools();
      }
      else {
        elementToolContainer.attach(_component);
      }
    }
    else if (value != null) {
      activateTools();
    }
  }
  
  private function activateTools():void {
    if (elementToolContainer.parent != _displayObjectContainer) {
      _displayObjectContainer.addChild(elementToolContainer);
      elementToolContainer.elementLayoutChangeListeners = new <ElementLayoutChangeListener>[selectionTool];
    }

    var dataContext:DataContext = DataManager.instance.getDataContext(DisplayObject(PlatformDataKeys.DOCUMENT.getData(DataManager.instance.getDataContext(_displayObjectContainer)).container));

    selectionTool.activate(elementToolContainer, _areaLocations, dataContext);
    elementToolContainer.attach(_component);

    for each (var tool:Tool in tools) {
      tool.activate(_displayObjectContainer, _areaLocations, dataContext);
    }
    
    // after attach all tools — toolContainer call all elementLayoutChangeListeners 
    //_toolContainer.attach(_component);
  }

  private function deactivateTools():void {
    elementToolContainer.detach();
    selectionTool.deactivate();

    for each (var tool:Tool in tools) {
      tool.deactivate();
    }
  }
}
}
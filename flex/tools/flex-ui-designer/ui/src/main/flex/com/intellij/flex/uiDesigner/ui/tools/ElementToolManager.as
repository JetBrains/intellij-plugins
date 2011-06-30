package com.intellij.flex.uiDesigner.ui.tools {
import org.flyti.plexus.Injectable;

public class ElementToolManager implements Injectable {
  private var tools:Vector.<Tool> = new <Tool>[new SelectionKnobsTool()];

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

  private var _element:Object;
  public function set element(value:Object):void {
    if (value == _element) {
      return;
    }

    if (_element != null) {
      detachTools();
    }
    
    _element = value;
    
    if (_element != null) {
      attachTools();
    }
  }
  
  private function attachTools():void {
    for each (var tool:Tool in tools) {
      tool.attach(_element, _toolContainer);
    }
    
    // after attach all tools — toolContainer call all elementLayoutChangeListeners 
    _toolContainer.attach(_element);
  }

  private function detachTools():void {
    for each (var tool:Tool in tools) {
      tool.detach();
    }
    
    _toolContainer.detach();
  }
}
}

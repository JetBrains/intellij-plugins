package com.intellij.flex.uiDesigner {
import cocoa.DataControl;

import org.flyti.plexus.Injectable;
import org.flyti.util.ArrayList;

public class StatesBarManager implements Injectable {
  private const source:Vector.<Object> = new Vector.<Object>();
  private const sourceList:ArrayList = new ArrayList(source);
  
  public function get states():Array {
    return _document.uiComponent.states;
  }
  
  public function set stateName(value:String):void {
    _document.uiComponent.currentState = value;
  }
  
  private var _document:Document;
  public function set document(value:Document):void {
    if (value == _document) {
      return;  
    }
    
    _document = value;
    if (_presentation == null) {
      return;
    }
    
    if (_document == null) {
      _presentation.hidden = true;
      return;
    }
    
    var documentStates:Array = states;
    _presentation.hidden = documentStates.length < 2;
    if (!_presentation.hidden) {
      updateStates(documentStates);
      var currentState:Object = _document.uiComponent.currentState;
      _presentation.selectedIndex = (currentState == null || currentState == "") ? 0 : sourceList.getItemIndex(currentState);
    }
  }

  private var _presentation:DataControl;
  public function set presentation(value:DataControl):void {
    if (_presentation != null) {
      _presentation.action = null;
    }

    _presentation = value;

    if (_presentation != null) {
      _presentation.action = changeHandler;
      _presentation.items = sourceList;
    }
  }
  
  private function updateStates(rawStates:Array):void {
    source.length = rawStates.length;
    for (var i:int = 0, n:int = rawStates.length; i < n; i++) {
      source[i] = rawStates[i].name;
    }

    sourceList.refresh();
  }

  private function changeHandler(item:String):void {
    _document.uiComponent.currentState = item;
  }
}
}

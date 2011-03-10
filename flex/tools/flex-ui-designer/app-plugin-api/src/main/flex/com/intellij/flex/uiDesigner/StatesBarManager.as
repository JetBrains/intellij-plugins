package com.intellij.flex.uiDesigner {
import cocoa.DataControl;

import mx.events.CollectionEvent;
import mx.events.CollectionEventKind;

import org.flyti.plexus.Injectable;
import org.flyti.util.ArrayList;

public class StatesBarManager implements Injectable {
  private const statesSource:Vector.<Object> = new Vector.<Object>();
  private const stateList:ArrayList = new ArrayList(statesSource);
  
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
    
    var documentStates:Array = states;
    _presentation.hidden = documentStates.length < 2;
    if (!_presentation.hidden) {
      updateStates(documentStates);
      var currentState:Object = _document.uiComponent.currentState;
      _presentation.selectedIndex = (currentState == null || currentState == "") ? 0 : stateList.getItemIndex(currentState);
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
      _presentation.items = stateList;
    }
  }
  
  private function updateStates(rawStates:Array):void {
    statesSource.length = rawStates.length;
    for (var i:int = 0, n:int = rawStates.length; i < n; i++) {
      statesSource[i] = rawStates[i].name;
    }

    stateList.refresh();
  }

  private function changeHandler(item:String):void {
    _document.uiComponent.currentState = item;
  }
}
}

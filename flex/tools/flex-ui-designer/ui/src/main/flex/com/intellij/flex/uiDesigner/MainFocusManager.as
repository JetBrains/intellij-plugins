package com.intellij.flex.uiDesigner {
import cocoa.AbstractFocusManager;
import cocoa.FocusManager;
import cocoa.Focusable;

import com.intellij.flex.uiDesigner.flex.DocumentFocusManagerSB;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;

import flash.display.InteractiveObject;
import flash.display.NativeWindow;
import flash.display.Stage;
import flash.events.Event;
import flash.events.MouseEvent;

public class MainFocusManager extends AbstractFocusManager implements FocusManager, MainFocusManagerSB {
  //noinspection JSFieldCanBeLocal
  private var cc:int;

  override protected function mouseDownHandler(event:MouseEvent):void {
    cc++;
    if (_activeDocumentFocusManager != null && _activeDocumentFocusManager.handleMouseDown(event)) {
      lastFocus = null;
    }
    else {
      var target:InteractiveObject = InteractiveObject(event.target);
      var newFocus:Focusable = getTopLevelFocusTarget(target);
      if (newFocus != lastFocus && newFocus != null) {
        lastFocus = newFocus;
        target.stage.focus = newFocus.focusObject;
      }
    }
  }

  private var _activeDocumentFocusManager:DocumentFocusManagerSB;
  public function set activeDocumentFocusManager(value:DocumentFocusManagerSB):void {
    _activeDocumentFocusManager = value;
    if (_activeDocumentFocusManager != null) {
      _activeDocumentFocusManager.restoreFocusToLastControl();
    }
  }

  override protected function windowActivateHandler(event:Event):void {
    var suggestedFocus:InteractiveObject;
    if (_activeDocumentFocusManager != null) {
      suggestedFocus = _activeDocumentFocusManager.restoreFocusToLastControl();
      if (suggestedFocus == null) {
        // activeDocumentFocusManager set focus to its object
        return;
      }
    }

    var stage:Stage = NativeWindow(event.currentTarget).stage;
    if (lastFocus != null) {
      stage.focus = lastFocus.focusObject;
    }
    else if (suggestedFocus != null) {
      stage.focus = suggestedFocus;
    }
  }
}
}

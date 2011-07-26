package com.intellij.flex.uiDesigner {
import cocoa.Focusable;

import com.intellij.flex.uiDesigner.flex.DocumentFocusManagerSB;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;

import flash.display.InteractiveObject;
import flash.display.NativeWindow;
import flash.display.Stage;
import flash.events.Event;
import flash.events.FocusEvent;
import flash.events.MouseEvent;
import flash.text.TextField;
import flash.text.TextFieldType;
import flash.ui.Keyboard;

import mx.managers.ISystemManager;

public class MainFocusManager implements MainFocusManagerSB {
  private var lastFocus:Focusable;

  public function MainFocusManager(stage:Stage) {
    init(stage);
  }

  private function init(stage:Stage):void {
    stage.addEventListener(FocusEvent.MOUSE_FOCUS_CHANGE, mouseFocusChangeHandler);
    stage.addEventListener(FocusEvent.KEY_FOCUS_CHANGE, keyFocusChangeHandler);

    stage.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);

    stage.nativeWindow.addEventListener(Event.ACTIVATE, windowActivateHandler);

    stage.stageFocusRect = false;
  }

  private function mouseDownHandler(event:MouseEvent):void {
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

  private static function getTopLevelFocusTarget(o:InteractiveObject):Focusable {
      while (!(o is ISystemManager)) {
        if (o is Focusable) {
          return Focusable(o);
        }

        if ((o = o.parent) == null) {
          break;
        }
      }

      return null;
    }

  private static function mouseFocusChangeHandler(event:FocusEvent):void {
    if (event.isDefaultPrevented()) {
      return;
    }

    if (event.relatedObject == null && event.isRelatedObjectInaccessible) {
      // lost focus to a control in different sandbox.
      return;
    }

    var textField:TextField = event.relatedObject as TextField;
    if (textField != null && (textField.type == TextFieldType.INPUT || textField.selectable)) {
      return; // pass it on
    }

    event.preventDefault();
  }

  private function keyFocusChangeHandler(event:FocusEvent):void {
    if (_activeDocumentFocusManager != null) {
      _activeDocumentFocusManager.showFocusIndicator = true;
    }

    // see if we got here from a tab
    if (event.keyCode == Keyboard.TAB && !event.isDefaultPrevented()) {
      //setFocusToNextObject(event);

      event.preventDefault();
    }
  }

  private function windowActivateHandler(event:Event):void {
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

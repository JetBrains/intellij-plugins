package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.flex.DocumentFocusManagerSB;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;

import flash.display.Stage;
import flash.events.Event;
import flash.events.FocusEvent;
import flash.text.TextField;
import flash.text.TextFieldType;
import flash.ui.Keyboard;

public class MainFocusManager implements MainFocusManagerSB {
  public function MainFocusManager(stage:Stage) {
    init(stage);
  }

  private function init(stage:Stage):void {
    stage.addEventListener(FocusEvent.MOUSE_FOCUS_CHANGE, mouseFocusChangeHandler);
    stage.addEventListener(FocusEvent.KEY_FOCUS_CHANGE, keyFocusChangeHandler);

    stage.nativeWindow.addEventListener(Event.ACTIVATE, windowActivateHandler);

    stage.stageFocusRect = false;
  }

  private var _activeDocumentFocusManager:DocumentFocusManagerSB;
  public function set activeDocumentFocusManager(value:DocumentFocusManagerSB):void {
    _activeDocumentFocusManager = value;
    if (_activeDocumentFocusManager != null) {
      _activeDocumentFocusManager.restoreFocusToLastControl();
    }
  }

  private static function mouseFocusChangeHandler(event:FocusEvent):void {
      if (event.isDefaultPrevented()) {
      return;
    }

    if (event.relatedObject == null && event.isRelatedObjectInaccessible) {
      // lost focus to a control in different sandbox.
      return;
    }

    var tf:TextField = event.relatedObject as TextField;
    if (tf != null && (tf.type == TextFieldType.INPUT || tf.selectable)) {
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

      // if we changed focus or if we're the main app eat the event
      event.preventDefault();
    }
  }

  private function windowActivateHandler(event:Event):void {
    if (_activeDocumentFocusManager != null) {
      _activeDocumentFocusManager.restoreFocusToLastControl();
    }
  }
}
}

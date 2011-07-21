package com.intellij.flex.uiDesigner.flex {
import flash.display.InteractiveObject;
import flash.events.MouseEvent;

public interface DocumentFocusManagerSB {
  function set showFocusIndicator(showFocusIndicator:Boolean):void;

  function restoreFocusToLastControl():InteractiveObject;

  function handleMouseDown(event:MouseEvent):Boolean;
}
}

package com.intellij.flex.uiDesigner.flex {
import flash.events.MouseEvent;

public interface DocumentFocusManagerSB {
  function set showFocusIndicator(showFocusIndicator:Boolean):void;

  function restoreFocusToLastControl():void;

  function handleMouseDown(event:MouseEvent):Boolean;
}
}

package com.intellij.flex.uiDesigner {
import cocoa.DesktopFocusManager;
import cocoa.FocusManager;
import cocoa.util.SharedPoint;

import com.intellij.flex.uiDesigner.flex.DocumentFocusManagerSB;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;

import flash.display.DisplayObject;

import flash.display.InteractiveObject;
import flash.display.NativeWindow;
import flash.display.Stage;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.geom.Point;

public class MainFocusManager extends DesktopFocusManager implements FocusManager, MainFocusManagerSB {
  private static var counter:int;
  
  override protected function mouseDownHandler(event:MouseEvent):void {
    if (_activeDocumentFocusManager != null && _activeDocumentFocusManager.handleMouseDown(event)) {
      lastFocus = null;
    }
    else {
      var objectsUnderPoint:Array = DisplayObject(event.currentTarget).stage.getObjectsUnderPoint(SharedPoint.mouseGlobal(event));
      counter++;
      trace(counter);
      super.mouseDownHandler(event);
    }

    //if (event.altKey) {
    //  var p:DisplayObjectContainer = InteractiveObject(event.target).stage;
    //  tC(p);
    //}
  }

  //private static function tC(p:DisplayObjectContainer):void {
  //  var childNumber:int = 0;
  //  while (true) {
  //    try {
  //      var child:DisplayObject = p.getChildAt(childNumber++);
  //      if (child is Shape) {
  //        var t:String = "33";
  //        t += "ss";
  //      }
  //      else if (child is DisplayObjectContainer && !(child is SegmentedControl)) {
  //        tC(DisplayObjectContainer(child));
  //      }
  //    }
  //    catch (e:RangeError) {
  //      break;
  //    }
  //  }
  //}

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

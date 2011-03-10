package com.intellij.flex.uiDesigner.flex {
import flash.desktop.NativeApplication;

import mx.core.FlexGlobals;
import mx.events.FlexEvent;

import spark.components.Application;

public class SparkApplication extends Application {
  public override function initialize():void {
    if (initialized) {
      return;
    }

    // The "preinitialize" event gets dispatched after everything about this
    // DisplayObject has been initialized, and it has been attached to
    // its parent, but before any of its children have been created.
    // This allows a "preinitialize" event handler to set properties which
    // affect child creation.
    // Note that this implies that "preinitialize" handlers are called
    // top-down; i.e., parents before children.
    dispatchEvent(new FlexEvent(FlexEvent.PREINITIALIZE));

    // Create child objects.

    createChildren();

    childrenCreated();

    // Create and initialize the accessibility implementation.
    // for this component. For some components accessible object is attached
    // to child component so it should be called after createChildren
    initializeAccessibility();

    // This should always be the last thing that initialize() calls.
    initializationComplete();
  }

  override public function get colorCorrection():String {
    return NativeApplication.nativeApplication.activeWindow.stage.colorCorrection;
  }

  override public function set colorCorrection(value:String):void {
  }
}
}

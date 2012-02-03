package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import flash.desktop.NativeApplication;
import flash.display.NativeWindow;
import flash.display.NativeWindowDisplayState;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataManager;

public final class ProjectUtil {
  public static function getRootDataContext():DataContext {
    var activeWindow:NativeWindow = NativeApplication.nativeApplication.activeWindow;
    if (activeWindow == null) {
      var openedWindows:Array = NativeApplication.nativeApplication.openedWindows;
      if (openedWindows.length != 0) {
        openedWindows.sort(compareWindows);
        activeWindow = openedWindows[openedWindows.length - 1];
      }
    }

    if (activeWindow == null || !(activeWindow is DocumentWindow) || activeWindow.stage.numChildren == 0) {
      return null;
    }
    else {
      return DataManager.instance.getDataContext(activeWindow.stage.getChildAt(0));
    }
  }

  private static function computeWindowPriority(w:NativeWindow):int {
    if (!(w is DocumentWindow)) {
      return -100;
    }

    var priority:int = 0;
    if (w.visible) {
      priority++;
    }
    if (w.displayState != NativeWindowDisplayState.MINIMIZED) {
      priority++;
    }

    return priority;
  }

  private static function compareWindows(a:NativeWindow, b:NativeWindow):int {
    return computeWindowPriority(a) - computeWindowPriority(b);
  }

  public static function getProjectForActiveWindow():Project {
    var rootDataContext:DataContext = getRootDataContext();
    return rootDataContext == null ? null : PlatformDataKeys.PROJECT.getData(rootDataContext);
  }
}
}
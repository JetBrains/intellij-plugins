package com.intellij.flex.uiDesigner {
import flash.desktop.NativeApplication;
import flash.display.NativeWindow;

import org.flyti.plexus.PlexusManager;

public final class ProjectUtil {
  public static function getProjectForActiveWindow():Project {
    var activeWindow:NativeWindow = NativeApplication.nativeApplication.activeWindow;
    if (activeWindow == null) {
      var openedWindows:Array = NativeApplication.nativeApplication.openedWindows;
      if (openedWindows.length != 0) {
        activeWindow = openedWindows[0];
      }
    }

    return ProjectManager(PlexusManager.instance.container.lookup(ProjectManager)).project;
  }
}
}
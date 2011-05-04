package com.intellij.flex.uiDesigner.plugins.test {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.Project;
import com.intellij.flex.uiDesigner.ProjectManager;

public class TestProjectManager extends ProjectManager {
  override protected function addNativeWindowListeners(window:DocumentWindow):void {
  }
  
  override protected function saveProjectWindowBounds(project:Project):void {
  }
}
}

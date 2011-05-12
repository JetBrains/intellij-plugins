package com.intellij.flex.uiDesigner.plugins.test {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.ModuleManager;
import com.intellij.flex.uiDesigner.Project;
import com.intellij.flex.uiDesigner.ProjectManager;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;

public class TestProjectManager extends ProjectManager {
  public function TestProjectManager(libraryManager:LibraryManager, moduleManager:ModuleManager) {
    super(libraryManager, moduleManager);
  }

  override protected function addNativeWindowListeners(window:DocumentWindow):void {
  }
  
  override protected function saveProjectWindowBounds(project:Project):void {
  }
}
}

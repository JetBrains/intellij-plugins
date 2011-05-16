package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.ModuleManager;
import com.intellij.flex.uiDesigner.ProjectManager;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;

public class TestProjectManager extends ProjectManager {
  internal var useRealProjectManagerBehavior:Boolean;

  public function TestProjectManager(libraryManager:LibraryManager, moduleManager:ModuleManager) {
    super(libraryManager, moduleManager);
  }
}
}

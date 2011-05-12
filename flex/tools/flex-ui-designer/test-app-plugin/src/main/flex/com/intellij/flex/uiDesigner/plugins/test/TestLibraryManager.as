package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;

public class TestLibraryManager extends LibraryManager {
  private static const FF:String = "mx.managers.LayoutManager";

  override protected function removeLibrarySet(librarySet:LibrarySet):void {
    // for tests, keep SDK
    if (librarySet.parent != null) {
      super.removeLibrarySet(librarySet);
    }
    else if (librarySet.applicationDomain.hasDefinition(FF)) {
      librarySet.applicationDomain.getDefinition(FF).prepareToDie();
    }
  }
}
}

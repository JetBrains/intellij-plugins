package com.intellij.javascript.flex.css;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;

/**
* @author Eugene.Kudelevsky
*/
class FlexInputFilter implements FileBasedIndex.InputFilter {

  private FlexInputFilter() {
  }

  private static class FlexInputFilterHolder {
    private static final FlexInputFilter ourInstance = new FlexInputFilter();
  }

  public static FlexInputFilter getInstance() {
    return FlexInputFilterHolder.ourInstance;
  }

  public boolean acceptInput(final VirtualFile file) {
    FileType type = file.getFileType();
    if (type == JavaScriptSupportLoader.JAVASCRIPT || type == FlexApplicationComponent.SWF_FILE_TYPE) return true;
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }
}

// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.css;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import org.jetbrains.annotations.NotNull;

/**
* @author Eugene.Kudelevsky
*/
final class FlexInputFilter extends DefaultFileTypeSpecificInputFilter {

  private FlexInputFilter() {
    super(ActionScriptFileType.INSTANCE, FlexApplicationComponent.SWF_FILE_TYPE, JavaScriptSupportLoader.getMxmlFileType());
  }

  private static class FlexInputFilterHolder {
    private static final FlexInputFilter ourInstance = new FlexInputFilter();
  }

  public static FlexInputFilter getInstance() {
    return FlexInputFilterHolder.ourInstance;
  }

  @Override
  public boolean acceptInput(@NotNull final VirtualFile file) {
    FileType type = file.getFileType();
    if (type == ActionScriptFileType.INSTANCE ||
        (type == FlexApplicationComponent.SWF_FILE_TYPE && file.getFileSystem() instanceof JarFileSystem)) {
      return true;
    }

    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }
}

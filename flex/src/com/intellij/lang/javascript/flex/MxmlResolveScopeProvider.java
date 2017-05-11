package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class MxmlResolveScopeProvider extends ActionScriptResolveScopeProvider {

  @Override
  protected boolean isApplicable(@NotNull final VirtualFile file) {
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }
}

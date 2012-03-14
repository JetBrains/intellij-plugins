package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveScopeProvider;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * User: ksafonov
 */
public class MxmlResolveScopeProvider extends ActionScriptResolveScopeProvider {

  @Override
  protected boolean isApplicable(final VirtualFile file) {
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }
}

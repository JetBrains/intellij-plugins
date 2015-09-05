package com.intellij.lang.javascript.flex.debug;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSElementFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// duplicates JSDebuggerEditorsProvider
class FlexDebuggerEditorsProvider extends XDebuggerEditorsProviderBase {
  @Override
  @NotNull
  public FileType getFileType() {
    return JavaScriptSupportLoader.JAVASCRIPT;
  }

  @Override
  protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String text, @Nullable PsiElement context, boolean isPhysical) {
    return JSElementFactory.createExpressionCodeFragment(project, text, context);
  }

  @Override
  protected PsiElement getContextElement(@NotNull VirtualFile virtualFile, int offset, @NotNull Project project) {
    return XDebuggerUtil.getInstance().findContextElement(virtualFile, offset, project, true);
  }
}
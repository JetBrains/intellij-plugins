package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.javascript.debugger.JavaScriptDebugAware;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.psi.DartId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DartWebDebugAware extends JavaScriptDebugAware {
  @NotNull
  @Override
  public FileType getFileType() {
    return DartFileType.INSTANCE;
  }

  @Nullable
  @Override
  public TextRange getRangeForNamedElement(@NotNull PsiElement element, @Nullable PsiElement parent, int offset) {
    if (parent instanceof DartId) {
      return parent.getTextRange().shiftRight(offset);
    }
    return null;
  }
}
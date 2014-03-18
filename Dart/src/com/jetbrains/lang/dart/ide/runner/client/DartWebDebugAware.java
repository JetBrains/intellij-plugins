package com.jetbrains.lang.dart.ide.runner.client;

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
    return DartFileType.INSTANCE; // todo return null in 13.1.1 (when JavaScriptDebugAware.isOnlySourceMappedBreakpoints() is introduced)
  }

  // todo uncomment in 13.1.1 (when JavaScriptDebugAware.isOnlySourceMappedBreakpoints() is introduced)
  //@Nullable
  //public XLineBreakpointType<?> getBreakpointTypeClass(@NotNull final Project project) {
  //  return XBreakpointType.EXTENSION_POINT_NAME.findExtension(DartLineBreakpointType.class);
  //}

  @Nullable
  @Override
  public TextRange getRangeForNamedElement(@NotNull PsiElement element, @Nullable PsiElement parent, int offset) {
    if (parent instanceof DartId) {
      return parent.getTextRange().shiftRight(offset);
    }
    return null;
  }
}
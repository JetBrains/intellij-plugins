package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.jetbrains.javascript.debugger.JavaScriptDebugAware;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import com.jetbrains.lang.dart.psi.DartId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DartWebDebugAware extends JavaScriptDebugAware {
  @Override
  public boolean isOnlySourceMappedBreakpoints() {
    return false;
  }

  @Override
  @Nullable
  public XLineBreakpointType<?> getBreakpointTypeClass(@NotNull final Project project) {
    return XBreakpointType.EXTENSION_POINT_NAME.findExtension(DartLineBreakpointType.class);
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
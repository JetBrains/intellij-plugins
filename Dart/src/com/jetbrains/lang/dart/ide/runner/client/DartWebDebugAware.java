package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.javascript.debugger.ExpressionInfoFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.ExpressionInfo;
import com.jetbrains.javascript.debugger.JavaScriptDebugAware;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DartWebDebugAware extends JavaScriptDebugAware {
  @Nullable
  @Override
  protected LanguageFileType getFileType() {
    return DartFileType.INSTANCE;
  }

  @Override
  public boolean isOnlySourceMappedBreakpoints() {
    return false;
  }

  @Override
  @Nullable
  public Class<? extends XLineBreakpointType<?>> getBreakpointTypeClass() {
    return DartLineBreakpointType.class;
  }

  @Nullable
  @Override
  public ExpressionInfo getEvaluationInfo(@NotNull PsiElement elementAtOffset,
                                          @NotNull Document document,
                                          @NotNull ExpressionInfoFactory expressionInfoFactory) {
    return DartVmServiceEvaluator.getExpressionInfo(elementAtOffset);
  }
}
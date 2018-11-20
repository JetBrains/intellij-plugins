// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

final class DartWebDebugAware extends JavaScriptDebugAware {
  @Override
  protected LanguageFileType getFileType() {
    return DartFileType.INSTANCE;
  }

  @Override
  public boolean isOnlySourceMappedBreakpoints() {
    return false;
  }

  @Override
  public Class<? extends XLineBreakpointType<?>> getBreakpointTypeClass() {
    return DartLineBreakpointType.class;
  }

  @Override
  public Promise<ExpressionInfo> getEvaluationInfo(@NotNull PsiElement element, @NotNull Document document, @NotNull ExpressionInfoFactory expressionInfoFactory) {
    return Promises.resolvedPromise(DartVmServiceEvaluator.getExpressionInfo(element));
  }
}
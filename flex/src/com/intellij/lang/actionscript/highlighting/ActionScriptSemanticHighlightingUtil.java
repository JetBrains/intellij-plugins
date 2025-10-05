// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.highlighting;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.openapi.application.ApplicationManager;

public final class ActionScriptSemanticHighlightingUtil {

  public static void highlight(JSAttribute attribute, AnnotationHolder holder) {
    AnnotationBuilder builder =
      ApplicationManager.getApplication().isUnitTestMode()
      ? holder.newAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY, "attribute")
      : holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY);
    builder.range(attribute).textAttributes(ECMAL4Highlighter.ECMAL4_METADATA).create();
  }

}

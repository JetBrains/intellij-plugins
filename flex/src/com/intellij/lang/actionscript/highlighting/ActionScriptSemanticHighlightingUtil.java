// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.highlighting;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingVisitor;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;

public final class ActionScriptSemanticHighlightingUtil {

  public static void highlight(JSAttribute attribute, AnnotationHolder holder) {
    JSSemanticHighlightingVisitor.lineMarker(attribute, ECMAL4Highlighter.ECMAL4_METADATA, "attribute", holder);
  }

}

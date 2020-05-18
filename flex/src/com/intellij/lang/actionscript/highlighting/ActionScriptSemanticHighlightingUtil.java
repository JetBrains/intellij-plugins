// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.actionscript.highlighting;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;

public class ActionScriptSemanticHighlightingUtil extends JSSemanticHighlightingUtil {

  public static void highlight(JSAttribute attribute, AnnotationHolder holder) {
    lineMarker(attribute, ECMAL4Highlighter.ECMAL4_METADATA, "attribute", holder);
  }

}

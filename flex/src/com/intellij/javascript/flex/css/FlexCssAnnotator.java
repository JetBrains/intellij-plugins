// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.css;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptTextReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssString;
import org.jetbrains.annotations.NotNull;

public class FlexCssAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    // skinClass: ClassReference("com.company.SomeClass");

    if (!(element instanceof CssString)) {
      return;
    }

    PsiReference[] references = element.getReferences();
    for (int i = 0, length = references.length; i < length; i++) {
      PsiReference reference = references[i];
      if (reference instanceof ActionScriptTextReference) {
        if (reference.resolve() == null) {
          String message = i == length - 1 ? FlexBundle.message("cannot.resolve.class.0", reference.getCanonicalText())
                                           : FlexBundle.message("cannot.resolve.package.0", reference.getCanonicalText());

          AnnotationBuilder builder = holder.newAnnotation(HighlightSeverity.ERROR, message).range(reference.getAbsoluteRange());
          for (LocalQuickFix fix : ((ActionScriptTextReference)reference).getQuickFixes()) {
            if (fix instanceof IntentionAction) {
              builder = builder.withFix((IntentionAction)fix);
            }
          }
          builder.create();
        }
      }
    }
  }
}

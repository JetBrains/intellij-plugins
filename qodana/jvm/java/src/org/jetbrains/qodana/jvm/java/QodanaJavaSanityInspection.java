// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.jvm.java;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.JavaResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.qodana.QodanaBundle;

final class QodanaJavaSanityInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitReferenceElement(@NotNull PsiJavaCodeReferenceElement reference) {
        if (JavaResolveUtil.isInJavaDoc(reference)) return;
        JavaResolveResult[] results = reference.multiResolve(false);
        if (results.length == 0) {
          String refName = reference.getReferenceName();
          holder.registerProblem(
            reference,
            QodanaBundle.message("inspection.message.unresolved.reference", refName),
            ProblemHighlightType.ERROR
          );
        }
      }
    };
  }
}

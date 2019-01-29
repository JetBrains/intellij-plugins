// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.angular2.inspections.quickfixes.RemoveAttributeQuickFix;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.jetbrains.annotations.NotNull;

public class Angular2TemplateReferenceVariableInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new Angular2HtmlElementVisitor() {

      @Override
      public void visitReference(Angular2HtmlReference reference) {
        String exportName = reference.getValue();
        if (exportName != null && !exportName.isEmpty()
            && reference.getVariable() != null
            && reference.getVariable().getType() == null) {
          holder.registerProblem(reference.getVariable(),
                                 "There is no directive with 'exportAs' set to '" + exportName +"'",
                                 new RemoveAttributeQuickFix(reference.getName()));
        }
      }
    };
  }
}

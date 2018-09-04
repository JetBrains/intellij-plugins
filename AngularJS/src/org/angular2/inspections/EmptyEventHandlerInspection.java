// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class EmptyEventHandlerInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new Angular2HtmlElementVisitor() {
      @Override
      public void visitEvent(Angular2HtmlEvent event) {
        if (event.getAction() == null) {
          holder.registerProblem(event, "Empty event handler attribute", new CreateAttributeQuickFix());
        }
      }
    };
  }

  private static class CreateAttributeQuickFix implements LocalQuickFix {
    @Nls
    @NotNull
    @Override
    public String getName() {
      return "Add attribute value";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return "AngularJS";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final XmlAttribute attribute = (XmlAttribute)descriptor.getPsiElement();
      attribute.setValue("");
      PsiNavigationSupport.getInstance().createNavigatable(project, attribute.getContainingFile().getVirtualFile(),
                                                           attribute.getValueElement().getTextRange()
                                                             .getStartOffset() + 1).navigate(true);
    }
  }
}

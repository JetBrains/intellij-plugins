// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class Angular2EmptyEventBindingInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    if (descriptor.getInfo().type == Angular2AttributeType.EVENT) {
      XmlAttributeValue value = attribute.getValueElement();
      if (value == null || value.getTextLength() == 0) {
        holder.registerProblem(attribute,
                               Angular2Bundle.message("angular.inspection.template.empty-event-binding-handler"),
                               new CreateAttributeQuickFix());
      }
      else if (value.getValue().trim().isEmpty()) {
        holder.registerProblem(value, Angular2Bundle.message("angular.inspection.template.empty-event-binding-handler"));
      }
    }
  }

  private static class CreateAttributeQuickFix implements LocalQuickFix {
    @Nls
    @NotNull
    @Override
    public String getName() {
      return Angular2Bundle.message("angular.quickfix.template.add-attribute-value.name");
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return Angular2Bundle.message("angular.quickfix.family");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final XmlAttribute attribute = (XmlAttribute)descriptor.getPsiElement();
      attribute.setValue("");
      final XmlAttributeValue value = attribute.getValueElement();
      if (value != null) {
        PsiNavigationSupport.getInstance().createNavigatable(
          project, attribute.getContainingFile().getVirtualFile(),
          value.getTextRange().getStartOffset() + 1
        ).navigate(true);
      }
    }
  }
}

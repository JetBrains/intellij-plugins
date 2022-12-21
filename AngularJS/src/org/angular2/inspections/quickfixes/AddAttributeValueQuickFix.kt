// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class AddAttributeValueQuickFix implements LocalQuickFix {
  @Override
  public @Nls @NotNull String getName() {
    return Angular2Bundle.message("angular.quickfix.template.add-attribute-value.name");
  }

  @Override
  public @Nls @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.template.add-attribute-value.family");
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

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class RemoveAttributeQuickFix implements LocalQuickFix {
  private final String myAttributeName;

  public RemoveAttributeQuickFix(@NotNull String name) {myAttributeName = name;}

  @Nls
  @NotNull
  @Override
  public String getName() {
    return Angular2Bundle.message("angular.quickfix.template.remove-attribute.name", myAttributeName);
  }

  @Nls
  @NotNull
  @Override
  public String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.template.remove-attribute.family");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final XmlAttribute attribute = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), XmlAttribute.class, false);
    if (attribute != null) {
      PsiElement parent = attribute.getParent();
      attribute.delete();
      FormatFixer.create(parent, FormatFixer.Mode.Reformat).fixFormat();
    }
  }
}

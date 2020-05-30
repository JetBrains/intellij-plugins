// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class RemoveTagContentQuickFix implements LocalQuickFix {
  @Override
  public @Nls @NotNull String getName() {
    return Angular2Bundle.message("angular.quickfix.template.remove-tag-content.name");
  }

  @Override
  public @Nls @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.template.remove-tag-content.family");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final XmlTag tag = (XmlTag)descriptor.getPsiElement();
    PsiElement[] content = tag.getValue().getChildren();
    if (content.length > 0) {
      tag.deleteChildRange(content[0], content[content.length - 1]);
    }
  }
}

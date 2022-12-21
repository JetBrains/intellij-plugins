// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class RemoveJSProperty implements LocalQuickFix {
  private final String myPropertyName;

  public RemoveJSProperty(@NotNull String name) {
    myPropertyName = name;
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getName() {
    return Angular2Bundle.message("angular.quickfix.decorator.remove-property.name", myPropertyName);
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.decorator.remove-property.family");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final JSProperty property = ObjectUtils.tryCast(descriptor.getPsiElement().getParent(), JSProperty.class);
    if (property != null) {
      PsiElement parent = property.getParent();
      property.delete();
      FormatFixer.create(parent, FormatFixer.Mode.Reformat).fixFormat();
    }
  }
}

// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;

public class CucumberAddExamplesColonFix implements LocalQuickFix {
  @Override
  public @NotNull String getFamilyName() {
    return CucumberBundle.message("intention.family.name.add.missing.after.examples.keyword");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiElement examples = descriptor.getPsiElement();
    final PsiElement[] elements = GherkinElementFactory.getTopLevelElements(project, ":");
    examples.getParent().addAfter(elements[0], examples);
  }
}

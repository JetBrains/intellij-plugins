// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.lang.java.JavaDocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationTargetProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.List;

/// Provides documentation for Gherkin steps by resolving them to their Java step definitions.
///
/// Invoked when the user triggers Quick Documentation on an already-typed step in a .feature file.
public final class CucumberJavaStepDocumentationTargetProvider implements DocumentationTargetProvider {
  @Override
  public @NotNull List<? extends @NotNull DocumentationTarget> documentationTargets(@NotNull PsiFile file, int offset) {
    PsiElement originalElement = file.findElementAt(offset);
    if (originalElement == null) return List.of();

    GherkinStep step = PsiTreeUtil.getParentOfType(originalElement, GherkinStep.class, false);
    if (step == null) return List.of();

    List<AbstractStepDefinition> definitions = new ArrayList<>(step.findDefinitions());
    if (definitions.isEmpty()) return List.of();

    AbstractStepDefinition definition = definitions.getFirst();
    PsiElement navigationElement = definition.getNavigationElement();
    if (navigationElement == null) return List.of();

    return List.of(new JavaDocumentationTarget(navigationElement, originalElement, false));
  }
}

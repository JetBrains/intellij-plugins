// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.lang.documentation.psi.UtilKt;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.List;

/// Provides documentation for Gherkin steps by resolving them to their Java step definitions.
///
/// It's invoked when the user triggers Quick Documentation on an already-typed ste in a .feature file.
public final class CucumberJavaStepDocumentationTargetProvider implements PsiDocumentationTargetProvider {
  @Override
  public @Nullable DocumentationTarget documentationTarget(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
    GherkinStep step = PsiTreeUtil.getParentOfType(originalElement, GherkinStep.class, false);
    if (step == null) return null;

    List<AbstractStepDefinition> definitions = new ArrayList<>(step.findDefinitions());
    if (definitions.isEmpty()) return null;

    AbstractStepDefinition definition = definitions.getFirst();
    PsiElement navigationElement = definition.getNavigationElement();
    if (navigationElement == null) return null;

    return UtilKt.createPsiDocumentationTarget(navigationElement, originalElement);
  }
}

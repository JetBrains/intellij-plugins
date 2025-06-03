// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;

public final class GherkinChangeUtil {
  public static @NotNull GherkinStep createStep(final String text, final Project project) {
    final GherkinFile dummyFile = createDummyFile(project,
                                                  "Feature: Dummy\n" +
                                                  "  Scenario: Dummy\n" +
                                                  "    " + text
    );
    final PsiElement feature = dummyFile.getFirstChild();
    final GherkinScenario scenario = PsiTreeUtil.getChildOfType(feature, GherkinScenario.class);
    final GherkinStep element = PsiTreeUtil.getChildOfType(scenario, GherkinStep.class);
    if (element == null) throw new IllegalStateException("element must not be null");
    return element;
  }

  public static GherkinFile createDummyFile(Project project, String text) {
    final String fileName = "dummy." + GherkinFileType.INSTANCE.getDefaultExtension();
    return (GherkinFile)PsiFileFactory.getInstance(project).createFileFromText(fileName, GherkinLanguage.INSTANCE, text);
  }
}

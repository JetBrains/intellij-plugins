// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;

public final class GherkinChangeUtil {
  @NotNull
  public static GherkinStep createStep(final String text, final Project project) {
    final GherkinFile dummyFile = createDummyFile(project,
                                                  "Feature: Dummy\n" +
                                                  "  Scenario: Dummy\n" +
                                                  "    " + text
    );

    final PsiElement feature = dummyFile.getFirstChild();
    assert feature != null;
    final GherkinScenario scenario = PsiTreeUtil.getChildOfType(feature, GherkinScenario.class);
    assert scenario != null;
    final GherkinStep element = PsiTreeUtil.getChildOfType(scenario, GherkinStep.class);
    assert element != null;
    return element;
  }

  public static GherkinFile createDummyFile(Project project, String text) {
    final String fileName = "dummy." + GherkinFileType.INSTANCE.getDefaultExtension();
    return (GherkinFile)PsiFileFactory.getInstance(project).createFileFromText(fileName, GherkinLanguage.INSTANCE, text);
  }
}

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

public final class GherkinChangeUtil {

  private GherkinChangeUtil() { }

  public static @NotNull GherkinStep createStep(String text, GherkinFile gherkinFile, Project project) {
    final String localeLanguage = gherkinFile.getLocaleLanguage();
    final GherkinKeywordProvider provider = JsonGherkinKeywordProvider.getKeywordProvider(gherkinFile);
    final GherkinKeywordTable table = provider.getKeywordsTable(localeLanguage);

    final String featureWord = table.getFeaturesSectionKeywords().iterator().next();
    final String scenarioWord = table.getScenarioKeywords().iterator().next();

    final String dummyFileText = String.format("""
                                                 #language: %s
                                                 %s: Dummy
                                                   %s: Dummy
                                                    \s""", localeLanguage, featureWord, scenarioWord) + text;

    final GherkinFile dummyFile = createDummyFile(project, dummyFileText);
    final GherkinFeature feature = PsiTreeUtil.getChildOfType(dummyFile, GherkinFeature.class);
    if (feature == null) throw new IllegalStateException("feature must not be null");
    final GherkinScenario scenario = PsiTreeUtil.getChildOfType(feature, GherkinScenario.class);
    if (scenario == null) throw new IllegalStateException("scenario must not be null");
    final GherkinStep step = PsiTreeUtil.getChildOfType(scenario, GherkinStep.class);
    if (step == null) throw new IllegalStateException("step must not be null");
    return step;
  }

  /**
   * @deprecated Use {@link #createStep(String, GherkinFile, Project)} instead.
   */
  @Deprecated(forRemoval = true)
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

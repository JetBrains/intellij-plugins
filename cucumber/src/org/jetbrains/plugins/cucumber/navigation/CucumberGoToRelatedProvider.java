// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.navigation;

import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CucumberGoToRelatedProvider extends GotoRelatedProvider {
  @Override
  public @NotNull List<? extends GotoRelatedItem> getItems(@NotNull DataContext context) {
    final PsiFile file = CommonDataKeys.PSI_FILE.getData(context);
    if (file != null) {
      return getItems(file);
    }
    return Collections.emptyList();
  }

  @Override
  public @NotNull List<? extends GotoRelatedItem> getItems(@NotNull PsiElement psiElement) {
    final PsiFile file = psiElement.getContainingFile();
    if (file instanceof GherkinFile gherkinFile) {
      final List<GherkinStep> steps = new ArrayList<>();
      final GherkinFeature[] features = gherkinFile.getFeatures();
      for (GherkinFeature feature : features) {
        final GherkinStepsHolder[] stepHolders = feature.getScenarios();
        for (GherkinStepsHolder stepHolder : stepHolders) {
          Collections.addAll(steps, stepHolder.getSteps());
        }
      }
      final List<PsiFile> resultFiles = new ArrayList<>();
      final List<GotoRelatedItem> result = new ArrayList<>();
      for (GherkinStep step : steps) {
        PsiElement stepDefMethod = CucumberUtil.resolveSep(step);
        if (stepDefMethod == null) {
          continue;
        }

        PsiFile stepDefFile = stepDefMethod.getContainingFile();
        if (!resultFiles.contains(stepDefFile)) {
          resultFiles.add(stepDefFile);
          result.add(new GotoRelatedItem(stepDefFile, CucumberBundle.message("create.step.definition.title")));
        }
      }
      return result;
    }
    return Collections.emptyList();
  }
}

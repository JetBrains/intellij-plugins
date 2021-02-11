// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import java.util.function.Consumer;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.calculateGlueFromGherkinFile;
import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.calculateGlueFromHooksAndTypes;

public class CucumberJavaFeatureGlueProvider implements CucumberGlueProvider {
  private final PsiElement myElement;

  public CucumberJavaFeatureGlueProvider(@NotNull PsiElement element) {
    myElement = element;
  }

  @Override
  public void calculateGlue(@NotNull Consumer<String> consumer) {
    PsiFile file = myElement.getContainingFile();
    calculateGlueFromHooksAndTypes(myElement, consumer);
    calculateGlueFromGherkinFile((GherkinFile)file, consumer);
  }
}

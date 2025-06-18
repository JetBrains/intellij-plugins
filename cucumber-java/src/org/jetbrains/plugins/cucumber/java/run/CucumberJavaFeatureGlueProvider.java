// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import java.util.function.Consumer;

public class CucumberJavaFeatureGlueProvider implements CucumberGlueProvider {
  private final PsiElement myElement;

  public CucumberJavaFeatureGlueProvider(@NotNull PsiElement element) {
    myElement = element;
  }

  /**
   * @param consumer Function that is called with the fully qualified name of a package containing Cucumber glue definitions.
   *                 If there is >1 such package, this function will be called for each one of them.
   */
  @Override
  public void calculateGlue(@NotNull Consumer<String> consumer) {
    PsiFile file = myElement.getContainingFile();
    CucumberJavaUtil.calculateGlueFromHooksAndTypes(myElement, consumer);
    CucumberJavaUtil.calculateGlueFromGherkinFile((GherkinFile)file, consumer);
  }
}

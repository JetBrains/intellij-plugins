// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public final class CucumberElementFactory {

  public static PsiElement createTempPsiFile(final @NotNull Project project, final @NotNull String text) {
    return PsiFileFactory.getInstance(project).createFileFromText("temp." + GherkinFileType.INSTANCE.getDefaultExtension(),
                                                                  GherkinFileType.INSTANCE,
                                                                  text, LocalTimeCounter.currentTime(), false);
  }
}

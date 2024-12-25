// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

public interface StepDefinitionCreator {
  /**
   * Creates step definition file
   * @param dir where to create file
   * @param name of created file
   * @return PsiFile object of created file
   */
  @NotNull
  PsiFile createStepDefinitionContainer(final @NotNull PsiDirectory dir, final @NotNull String name);

  /**
   * Creates step definition
   * @param step to implement
   * @param file where to create step definition
   * @param withTemplate should or not run template builder around regex and step body. We should not force user to go through
   *                     number of templates in case of "Create All Step Definitions" action invoked
   * @return true if success, false otherwise
   */
  default boolean createStepDefinition(final @NotNull GherkinStep step, final @NotNull PsiFile file, boolean withTemplate) {
    return false;
  }

  /**
   * Validates name of new step definition file
   * @param fileName name of file to check
   * @return true if name is valid, false otherwise
   */
  default boolean validateNewStepDefinitionFileName(final @NotNull Project project, final @NotNull String fileName) {
    return true;
  }

  @NotNull
  String getDefaultStepDefinitionFolderPath(final @NotNull GherkinStep step);

  /**
   * @return step definition file path relative to step definition folder
   */
  @NlsSafe
  @NotNull
  String getStepDefinitionFilePath(final @NotNull PsiFile file);

  /**
   * Provides default name of step definition file
   * @param step step we want to create definition container for
   * @return String representing default name of step definition file
   */
  @NotNull String getDefaultStepFileName(@NotNull GherkinStep step);
}

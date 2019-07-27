package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.ApiStatus;
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
  PsiFile createStepDefinitionContainer(@NotNull final PsiDirectory dir, @NotNull final String name);

  /**
   * Creates step definition. Use @{code StepDefinitionCreator#createStepDefinition(GherkinStep, PsiFile, boolean)} instead
   * @param step to implement
   * @param file where to create step definition
   * @return true if success, false otherwise
   */
  @Deprecated
  @ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
  default boolean createStepDefinition(@NotNull final GherkinStep step, @NotNull final PsiFile file) {
    return false;
  }

  /**
   * Creates step definition
   * @param step to implement
   * @param file where to create step definition
   * @param withTemplate should or not run template builder around regex and step body. We should not force user to go through
   *                     number of templates in case of "Create All Step Definitions" action invoked
   * @return true if success, false otherwise
   */
  default boolean createStepDefinition(@NotNull final GherkinStep step, @NotNull final PsiFile file, boolean withTemplate) {
    return createStepDefinition(step, file);
  }

  /**
   * Validates name of new step definition file
   * @param fileName name of file to check
   * @return true if name is valid, false otherwise
   */
  boolean validateNewStepDefinitionFileName(@NotNull final Project project, @NotNull final String fileName);

  @NotNull
  PsiDirectory getDefaultStepDefinitionFolder(@NotNull final GherkinStep step);

  @NotNull
  String getStepDefinitionFilePath(@NotNull final PsiFile file);

  /**
   * Provides default name of step definition file
   * @param step step we want to create definition container for
   * @return String representing default name of step definition file
   */
  @NotNull String getDefaultStepFileName(@NotNull GherkinStep step);
}

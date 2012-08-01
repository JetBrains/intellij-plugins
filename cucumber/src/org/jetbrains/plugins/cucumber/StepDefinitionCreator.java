package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

/**
 * User: Andrey.Vokin
 * Date: 8/1/12
 */
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
   * Creates step definition
   * @param step to implement
   * @param file where to create step definition
   * @return true if success, false otherwise
   */
  boolean createStepDefinition(@NotNull final GherkinStep step, @NotNull final PsiFile file);

  /**
   * Validates name of new step definition file
   * @param fileName name of file to check
   * @return true if name is valid, false otherwise
   */
  boolean validateNewStepDefinitionFileName(@NotNull final Project project, @NotNull final String fileName);

}

package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 12/13/10
 */
public interface CucumberJvmExtensionPoint {

  ExtensionPointName<CucumberJvmExtensionPoint> EP_NAME =
    ExtensionPointName.create("org.jetbrains.plugins.ruby.cucumber.steps.cucumberJvmExtensionPoint");

  // ToDo: remove parent
  /**
   * Checks if the child could be step definition file
   * @param child a PsiFile
   * @param parent container of the child
   * @return true if the child could be step definition file, else otherwise
   */
  boolean isStepLikeFile(@NotNull final PsiElement child, @NotNull final PsiElement parent);

  /**
   * Parses psiFile and creates list of step definition
   * @param psiFile file to parse
   * @return list of step definitions
   */
  @NotNull List<AbstractStepDefinition> getStepDefinitions(@NotNull final PsiFile psiFile);

  /**
   * Creates step definition
   * @param step to implement
   * @param file where to create step definition
   * @return true if success, false otherwise
   */
  boolean createStepDefinition(@NotNull final GherkinStep step, @NotNull final PsiFile file);

  /**
   * Provides type of step definition file
   * @return FileType
   */
  @NotNull FileType getStepFileType();

  /**
   * Creates step definition file
   * @param dir where to create file
   * @param name of created file
   * @return PsiFile object of created file
   */
  @NotNull PsiFile createStepDefinitionFile(@NotNull final PsiDirectory dir, @NotNull final String name);

  /**
   * Provides default name of step definition file
   * @return String representing default name of step definition file
   */
  @NotNull String getDefaultStepFileName();

  /**
   * Validates name of new step definition file
   * @param fileName name of file to check
   * @return true if name is valid, false otherwise
   */
  boolean validateNewStepDefinitionFileName(@NotNull final Project project, @NotNull final String fileName);
}

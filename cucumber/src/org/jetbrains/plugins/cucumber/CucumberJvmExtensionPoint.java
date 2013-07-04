package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: Andrey.Vokin
 * Date: 12/13/10
 */
public interface CucumberJvmExtensionPoint {
  ExtensionPointName<CucumberJvmExtensionPoint> EP_NAME =
    ExtensionPointName.create("org.jetbrains.plugins.cucumber.steps.cucumberJvmExtensionPoint");

  // ToDo: remove parent
  /**
   * Checks if the child could be step definition file
   * @param child a PsiFile
   * @param parent container of the child
   * @return true if the child could be step definition file, else otherwise
   */
  boolean isStepLikeFile(@NotNull final PsiElement child, @NotNull final PsiElement parent);

  /**
   * Checks if the child could be a step definition container
   * @param child PsiElement to check
   * @param parent it's container
   * @return true if child could be step definition container and it's possible to write in it
   */
  boolean isWritableStepLikeFile(@NotNull final PsiElement child, @NotNull final PsiElement parent);

  /**
   * Provides type of step definition file
   * @return FileType
   */
  @NotNull
  FileType getStepFileType();


  @NotNull
  StepDefinitionCreator getStepDefinitionCreator();

  /**
   * Provide resolving of step
   * @param step to be resolved
   * @return list of elements where step is resolved
   */
  List<PsiElement> resolveStep(@NotNull final PsiElement step);

  /**
   * Infers all 'glue' parameters for the file which it can find out.
   * @return inferred 'glue' parameters
   */
  @NotNull
  Collection<String> getGlues(@NotNull GherkinFile file, Set<String> gluesFromOtherFiles);

  /**
   * Provides all possible step definitions available from current feature file.
   * @param featureFile
   * @param module
   * @return
   */
  List<AbstractStepDefinition> loadStepsFor(@Nullable final PsiFile featureFile, @NotNull final Module module);

  void flush();

  void reset();

  void init(@NotNull final Project project);

  Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull final GherkinFile file);
}

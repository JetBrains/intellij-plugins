package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.Collection;
import java.util.List;

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
  boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent);

  /**
   * Checks if the child could be a step definition container
   * @param child PsiElement to check
   * @param parent it's container
   * @return true if child could be step definition container and it's possible to write in it
   */
  boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent);

  /**
   * Provides type of step definition file
   * @return type
   */
  @NotNull
  BDDFrameworkType getStepFileType();


  @NotNull
  StepDefinitionCreator getStepDefinitionCreator();

  /**
   * Provides all possible step definitions available from current feature file.
   * @param featureFile
   * @param module
   * @return
   */
  List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module);

  @ApiStatus.ScheduledForRemoval(inVersion = "2020.1")
  @Deprecated
  void flush(@NotNull Project project);

  @ApiStatus.ScheduledForRemoval(inVersion = "2020.1")
  @Deprecated
  void reset(@NotNull Project project);

  @ApiStatus.ScheduledForRemoval(inVersion = "2020.1")
  @Deprecated
  Object getDataObject(@NotNull Project project);

  Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull GherkinFile file);
  
  default boolean isGherkin6Supported(@NotNull Module module) {
    return false;
  }
  
  @Nullable
  default String getStepName(@NotNull PsiElement step) {
    if (!(step instanceof GherkinStepImpl)) {
      return null;
    }
    return ((GherkinStepImpl)step).getSubstitutedName();
  }
}

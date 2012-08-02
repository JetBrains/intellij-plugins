package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

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
   * Parses psiFile and creates list of step definition
   * @param psiFile file to parse
   * @return list of step definitions
   */
  @NotNull List<AbstractStepDefinition> getStepDefinitions(@NotNull final PsiFile psiFile);

  /**
   * Provides type of step definition file
   * @return FileType
   */
  @NotNull FileType getStepFileType();


  @NotNull
  StepDefinitionCreator getStepDefinitionCreator();

  /**
   * Provides default name of step definition file
   * @return String representing default name of step definition file
   */
  @NotNull String getDefaultStepFileName();


  void collectAllStepDefsProviders(@NotNull final List<VirtualFile> providers, @NotNull final Project project);

  boolean isStepDefinitionsRoot(@NotNull final VirtualFile file, @NotNull final Module module);

  void loadStepDefinitionRootsFromLibraries(Module module, final List<PsiDirectory> newAbstractStepDefinitionsRoots,
                                            @NotNull final Set<String> processedStepDirectories);

  ResolveResult[] resolveStep(@NotNull final PsiElement step);

  public void findRelatedStepDefsRoots(Module module, final PsiFile featureFile, final List<PsiDirectory> newAbstractStepDefinitionsRoots,
                                       final Set<String> processedStepDirectories);
}

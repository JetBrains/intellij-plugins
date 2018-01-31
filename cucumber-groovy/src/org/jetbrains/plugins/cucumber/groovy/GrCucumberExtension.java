package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinition;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.NotIndexedCucumberExtension;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Max Medvedev
 */
public class GrCucumberExtension extends NotIndexedCucumberExtension {
  @Override
  public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return child instanceof GroovyFile && ((GroovyFile)child).getName().endsWith(".groovy");
  }

  @Override
  public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return isStepLikeFile(child, parent);
  }

  @NotNull
  @Override
  public BDDFrameworkType getStepFileType() {
    return new BDDFrameworkType(GroovyFileType.GROOVY_FILE_TYPE);
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new GrStepDefinitionCreator();
  }

  @Nullable
  private String getGlue(PsiElement stepDefinition) {
    if (stepDefinition instanceof GrMethodCall) {
      GroovyFile groovyFile = (GroovyFile)stepDefinition.getContainingFile();
      VirtualFile vfile = groovyFile.getVirtualFile();
      if (vfile != null) {
        VirtualFile parentDir = vfile.getParent();
        return PathUtil.getLocalPath(parentDir);
      }
    }
    return null;
  }

  @NotNull
  @Override
  public Collection<String> getGlues(@NotNull GherkinFile file, Set<String> gluesFromOtherFiles) {
    if (gluesFromOtherFiles == null) {
      gluesFromOtherFiles = ContainerUtil.newHashSet();
    }
    final Set<String> glues = gluesFromOtherFiles;

    for (AbstractStepDefinition stepDefinition : getAllStepDefinitions(file.getProject())) {
      final PsiElement stepDefinitionElement = stepDefinition.getElement();
      final String glue = getGlue(stepDefinitionElement);
      if (glue != null) {
        glues.add(glue);
      }
    }

    return glues;
  }

  @Override
  protected void loadStepDefinitionRootsFromLibraries(Module module, List<PsiDirectory> roots, Set<String> directories) {

  }

  @NotNull
  @Override
  public List<AbstractStepDefinition> getStepDefinitions(@NotNull PsiFile psiFile) {
    final List<AbstractStepDefinition> newDefs = new ArrayList<>();
    if (psiFile instanceof GroovyFile) {
      GrStatement[] statements = ((GroovyFile)psiFile).getStatements();
      for (GrStatement statement : statements) {
        if (GrCucumberUtil.isStepDefinition(statement)) {
          newDefs.add(GrStepDefinition.getStepDefinition((GrMethodCall)statement));
        }
      }
    }
    return newDefs;
  }

  @Override
  protected void collectAllStepDefsProviders(@NotNull List<VirtualFile> providers, @NotNull Project project) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      if (ModuleType.get(module) instanceof JavaModuleType) {
        final VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        ContainerUtil.addAll(providers, roots);
      }
    }
  }


  @Override
  public void findRelatedStepDefsRoots(@NotNull final Module module, @NotNull final PsiFile featureFile,
                                       List<PsiDirectory> newStepDefinitionsRoots, Set<String> processedStepDirectories) {
    // ToDo: check if inside test folder
    for (VirtualFile sfDirectory : ModuleRootManager.getInstance(module).getSourceRoots()) {
      if (sfDirectory.isDirectory()) {
        PsiDirectory sourceRoot = PsiDirectoryFactory.getInstance(module.getProject()).createDirectory(sfDirectory);
        if (!processedStepDirectories.contains(sourceRoot.getVirtualFile().getPath())) {
          newStepDefinitionsRoots.add(sourceRoot);
        }
      }
    }
  }
}

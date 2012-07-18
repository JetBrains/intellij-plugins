package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberJvmExtensionPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: Andrey.Vokin
 * Date: 7/16/12
 */
public class JavaCucumberExtension implements CucumberJvmExtensionPoint {
  @Override
  public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return child instanceof PsiJavaFile;
  }

  @NotNull
  @Override
  public List<AbstractStepDefinition> getStepDefinitions(@NotNull PsiFile psiFile) {
    final Project project = psiFile.getProject();
    final List<AbstractStepDefinition> newDefs = new ArrayList<AbstractStepDefinition>();
    psiFile.acceptChildren(new JavaRecursiveElementVisitor() {

      @Override
      public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        newDefs.add(new JavaStepDefinition(method));
      }
    });
    return newDefs;
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public FileType getStepFileType() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public PsiFile createStepDefinitionFile(@NotNull PsiDirectory dir, @NotNull String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getDefaultStepFileName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String fileName) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void collectAllStepDefsProviders(@NotNull List<VirtualFile> providers, @NotNull Project project) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isStepDefinitionsRoot(@NotNull VirtualFile file) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void loadStepDefinitionRootsFromLibraries(Module module,
                                                   boolean excludeAlreadyLoadedRoots,
                                                   List<PsiDirectory> newAbstractStepDefinitionsRoots,
                                                   @NotNull Set<String> processedStepDirectories) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isInStepDefinitionDirectory(@NotNull PsiDirectory dir) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ResolveResult[] resolveStep(@NotNull PsiElement step) {
    return new ResolveResult[0];  //To change body of implemented methods use File | Settings | File Templates.
  }
}

package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

/**
 * User: Andrey.Vokin
 * Date: 8/1/12
 */
public class JavaStepDefinitionCreator implements StepDefinitionCreator {
  @NotNull
  @Override
  public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String fileName) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public PsiDirectory getDefaultStepDefinitionFolder(@NotNull GherkinStep step) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getStepDefinitionFilePath(@NotNull VirtualFile file) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}

package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;

/**
 * User: avokin
 * Date: 10/12/12
 */
public class CucumberJavaAllFeaturesInFolderRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected String getGlue() {
    return " --glue org.test";
  }

  @Override
  protected String getName() {
    return CucumberBundle.message("cucumber.run.all.features", ((PsiDirectory) mySourceElement).getVirtualFile().getName());
  }

  @NotNull
  @Override
  protected VirtualFile getFileToRun() {
    return ((PsiDirectory) mySourceElement).getVirtualFile();
  }

  protected boolean isApplicable(PsiElement locationElement, final Module module) {
    return locationElement != null && locationElement instanceof PsiDirectory;
  }
}

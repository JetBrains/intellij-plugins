package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit2.info.LocationUtil;
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
    return null;
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

  @Override
  protected RunnerAndConfigurationSettings createConfiguration(Location location, ConfigurationContext context) {
    if (!LocationUtil.isJarAttached(location, CUCUMBER_MAIN_CLASS, new PsiDirectory[] {(PsiDirectory)mySourceElement})) return null;
    RunnerAndConfigurationSettings result = super.createConfiguration(location, context);
    CucumberJavaRunConfiguration runConfiguration = (CucumberJavaRunConfiguration)result.getConfiguration();
    runConfiguration.getEnvs().put("current_dir", getFileToRun().getPath());
    return result;
  }
}

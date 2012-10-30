package org.jetbrains.plugins.cucumber.groovy.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfiguration;

/**
 * @author Max Medvedev
 */

public class GrCucumberRunConfiguration extends GroovyScriptRunConfiguration {
  private String myFeaturePath;

  public GrCucumberRunConfiguration(String name, Project project, ConfigurationFactory factory) {
    super(name, project, factory);
  }

  @Nullable
  private VirtualFile getFeatureVirtualFile() {
    if (myFeaturePath == null) return null;
    return LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(myFeaturePath));
  }

  @Nullable
  private GherkinFile getFeatureFile() {
    final VirtualFile vfile = getFeatureVirtualFile();
    if (vfile == null) return null;

    final PsiFile file = PsiManager.getInstance(getProject()).findFile(vfile);
    return file instanceof GherkinFile ? (GherkinFile)file : null;
  }

  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new GrCucumberRunConfigurationEditor();
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (StringUtil.isEmptyOrSpaces(myFeaturePath)) {
      throw new RuntimeConfigurationWarning(GrCucumberBundle.message("feature.path.is.empty"));
    }
    final GherkinFile featureFile = getFeatureFile();
    if (featureFile == null) {
      throw new RuntimeConfigurationWarning(GrCucumberBundle.message("feature.file.does.not.exist"));
    }

    super.checkConfiguration();
  }


  public void setFeaturePath(@Nullable String featurePath) {
    myFeaturePath = featurePath;
  }

  @Nullable
  public String getFeaturePath() {
    return myFeaturePath;
  }
}

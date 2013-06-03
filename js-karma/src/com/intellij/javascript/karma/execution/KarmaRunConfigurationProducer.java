package com.intellij.javascript.karma.execution;

import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RuntimeConfiguration;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunConfigurationProducer extends RuntimeConfigurationProducer {

  private PsiElement mySourceElement;

  public KarmaRunConfigurationProducer() {
    super(KarmaConfigurationType.getInstance());
  }

  @Override
  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Nullable
  @Override
  protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
    RuntimeConfiguration original = context.getOriginalConfiguration(null);
    if (original != null && !ConfigurationTypeUtil.equals(original.getType(), KarmaConfigurationType.getInstance())) {
      return null;
    }
    JSFile jsFile = getConfigJsFile(location);
    if (jsFile == null) {
      return null;
    }
    mySourceElement = jsFile;

    final RunnerAndConfigurationSettings runnerSettings = cloneTemplateConfiguration(location.getProject(), context);
    KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(runnerSettings.getConfiguration(), KarmaRunConfiguration.class);
    if (runConfiguration == null) {
      return null;
    }

    VirtualFile vFile = jsFile.getVirtualFile();
    if (vFile == null) {
      return null;
    }
    String path = FileUtil.toSystemDependentName(vFile.getPath());
    KarmaRunSettings settings = new KarmaRunSettings.Builder().setConfigPath(path).build();
    runConfiguration.setRunSettings(settings);

    String name = runConfiguration.suggestedName();
    runConfiguration.setName(name);
    runnerSettings.setName(name);

    return runnerSettings;
  }

  @Nullable
  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(Location location,
                                                                 @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 ConfigurationContext context) {
    JSFile jsFile = getConfigJsFile(location);
    if (jsFile == null) {
      return null;
    }
    VirtualFile vFile = jsFile.getVirtualFile();
    if (vFile == null) {
      return null;
    }
    String path = FileUtil.toSystemDependentName(vFile.getPath());
    KarmaRunSettings runSettingsPattern = new KarmaRunSettings.Builder().setConfigPath(path).build();
    for (RunnerAndConfigurationSettings candidateRaCSettings : existingConfigurations) {
      KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(candidateRaCSettings.getConfiguration(), KarmaRunConfiguration.class);
      if (runConfiguration != null) {
        KarmaRunSettings runSettingsCandidate = runConfiguration.getRunSetting();
        if (runSettingsCandidate.getConfigPath().equals(runSettingsPattern.getConfigPath())) {
          return candidateRaCSettings;
        }
      }
    }
    return null;
  }

  @Nullable
  private static JSFile getConfigJsFile(@NotNull Location location) {
    PsiElement element = location.getPsiElement();
    final JSFile jsFile;
    if (element instanceof PsiFile) {
      jsFile = ObjectUtils.tryCast(element, JSFile.class);
    }
    else {
      jsFile = ObjectUtils.tryCast(element.getContainingFile(), JSFile.class);
    }
    if (jsFile != null && jsFile.getName().endsWith(".conf.js")) {
      return jsFile;
    }
    return null;
  }

  @Override
  public int compareTo(Object o) {
    return PREFERED;
  }
}

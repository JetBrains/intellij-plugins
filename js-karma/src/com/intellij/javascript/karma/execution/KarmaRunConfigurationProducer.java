package com.intellij.javascript.karma.execution;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.Ref;
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
public class KarmaRunConfigurationProducer extends RunConfigurationProducer<KarmaRunConfiguration> {

  public KarmaRunConfigurationProducer() {
    super(KarmaConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(KarmaRunConfiguration configuration,
                                                  ConfigurationContext context,
                                                  Ref<PsiElement> sourceElement) {
    if (configuration == null) {
      return false;
    }
    RunConfiguration original = context.getOriginalConfiguration(null);
    if (original != null && !ConfigurationTypeUtil.equals(original.getType(), KarmaConfigurationType.getInstance())) {
      return false;
    }

    JSFile jsFile = getConfigJsFile(context.getLocation());
    if (jsFile == null) {
      return false;
    }

    VirtualFile configVirtualFile = jsFile.getVirtualFile();
    if (configVirtualFile == null) {
      return false;
    }

    sourceElement.set(jsFile);
    setupKarmaConfiguration(configuration, configVirtualFile);

    return true;
  }

  private static void setupKarmaConfiguration(@NotNull KarmaRunConfiguration configuration, @NotNull VirtualFile configVirtualFile) {
    String path = FileUtil.toSystemDependentName(configVirtualFile.getPath());
    KarmaRunSettings settings = new KarmaRunSettings.Builder().setConfigPath(path).build();
    configuration.setRunSettings(settings);

    String name = configuration.suggestedName();
    configuration.setName(name);
  }

  @Override
  public boolean isConfigurationFromContext(KarmaRunConfiguration configuration, ConfigurationContext context) {
    if (configuration == null) {
      return false;
    }
    JSFile jsFile = getConfigJsFile(context.getLocation());
    if (jsFile == null) {
      return false;
    }
    VirtualFile configVirtualFile = jsFile.getVirtualFile();
    if (configVirtualFile == null) {
      return false;
    }
    String path = FileUtil.toSystemDependentName(configVirtualFile.getPath());
    KarmaRunSettings runSettingsPattern = new KarmaRunSettings.Builder().setConfigPath(path).build();

    KarmaRunSettings runSettingsCandidate = configuration.getRunSetting();
    return runSettingsCandidate.getConfigPath().equals(runSettingsPattern.getConfigPath());
  }

  @Nullable
  private static JSFile getConfigJsFile(@Nullable Location location) {
    if (location == null) {
      return null;
    }
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
  public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
    return true;
  }
}

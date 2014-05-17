package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.PhoneGapConfigurationEditor;

/**
 * PhoneGapRunConfiguration.java
 *
 * Created by Masahiro Suzuka on 2014/04/05.
 */
public class PhoneGapRunConfiguration extends RunConfigurationBase {

  public static String PHONEGAP_PATH = "phonegap";
  public static String PHONEGAP_COMMAND = "run";
  public static String PHONEGAP_PLATFORM = "android"; // Default value

  public PhoneGapRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new PhoneGapConfigurationEditor(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (!(PHONEGAP_PATH.equals(PhoneGapSettings.PHONEGAP_PATH) || PHONEGAP_PATH.equals(PhoneGapSettings.CORDOVA_PATH))) {
      throw new RuntimeConfigurationException("SDK is missing");
    }

    if (!(PHONEGAP_COMMAND.equals("run"))) {
      throw new RuntimeConfigurationException("Command is missing");
    }

    if ( !(PHONEGAP_PLATFORM.equals("android") ||
        PHONEGAP_PLATFORM.equals("ios") ||
        PHONEGAP_PLATFORM.equals("windowsphone")||
        PHONEGAP_PLATFORM.equals("ripple")) ) {
      throw new RuntimeConfigurationException("Platform is missing");
    }
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor,
                                  @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
    PhoneGapRunProfileState state = new PhoneGapRunProfileState(getProject(),
        executionEnvironment,
        this);

    return state;
  }
}

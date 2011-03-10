package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.config.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class FlexUnitConsoleProperties extends TestConsoleProperties {
  private final RuntimeConfiguration myConfig;

  public FlexUnitConsoleProperties(final FlexUnitRunConfiguration config, Executor executor) {
    super(new Storage.PropertiesComponentStorage("FlexUnitSupport.", PropertiesComponent.getInstance()), config.getProject(), executor);
    myConfig = new FlexUnitDelegatingRuntimeConfiguration(config);
  }

  public RuntimeConfiguration getConfiguration() {
    return myConfig;
  }

  public static class FlexUnitDelegatingRuntimeConfiguration extends RuntimeConfiguration {
    private final FlexUnitRunConfiguration myConfig;

    public FlexUnitDelegatingRuntimeConfiguration(FlexUnitRunConfiguration config) {
      super(config.getName(), config.getProject(), config.getFactory());
      myConfig = config;
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
      return myConfig.getConfigurationEditor();
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone"})
    @Override
    public RuntimeConfiguration clone() {
      return new FlexUnitDelegatingRuntimeConfiguration(myConfig.clone());
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
      return myConfig.getState(executor, env);
    }

    @NotNull
      @Override
      public Module[] getModules() {
      return myConfig.getModules();
    }

    @Override
      public void checkConfiguration() throws RuntimeConfigurationException {
      myConfig.checkConfiguration();
    }

    @Override
      public boolean isGeneratedName() {
      return myConfig.isGeneratedName();
    }

    @Override
      public String suggestedName() {
      return myConfig.suggestedName();
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
      myConfig.readExternal(element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
      myConfig.writeExternal(element);
    }

    public FlexUnitRunConfiguration getPeer() {
      return myConfig;
    }
  }
}

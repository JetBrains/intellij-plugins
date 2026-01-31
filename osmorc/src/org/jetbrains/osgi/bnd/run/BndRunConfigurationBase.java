// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;

public abstract class BndRunConfigurationBase extends LocatableConfigurationBase<BndRunConfigurationOptions>
  implements ModuleRunProfile, PersistentStateComponent<BndRunConfigurationOptions> {
  public BndRunConfigurationBase(Project project, @NotNull ConfigurationFactory factory, String name) {
    super(project, factory, name);
  }

  @Override
  protected @NotNull BndRunConfigurationOptions getOptions() {
    return (BndRunConfigurationOptions)super.getOptions();
  }

  @Override
  public @NotNull SettingsEditor<? extends BndRunConfigurationBase> getConfigurationEditor() {
    return new BndRunConfigurationEditor(getProject());
  }

  @Override
  public abstract @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException;

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    String file = getOptions().getBndRunFile();
    if (file == null || !new File(file).isFile()) {
      throw new RuntimeConfigurationException(OsmorcBundle.message("bnd.run.configuration.invalid", file));
    }
    if (getOptions().getUseAlternativeJre()) {
      JavaParametersUtil.checkAlternativeJRE(getOptions().getAlternativeJrePath());
    }
  }

  public static class Launch extends BndRunConfigurationBase {
    public Launch(Project project, @NotNull ConfigurationFactory factory, String name) {
      super(project, factory, name);
    }

    @Override
    public @Nullable BndLaunchState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
      return new BndLaunchState(environment, this);
    }
  }

  public static class Test extends BndRunConfigurationBase {
    public Test(Project project, @NotNull ConfigurationFactory factory, String name) {
      super(project, factory, name);
    }

    @Override
    public @Nullable BndTestState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
      return new BndTestState(environment, this);
    }
  }
}

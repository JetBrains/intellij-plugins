// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;

public abstract class BndRunConfigurationBase extends LocatableConfigurationBase implements ModuleRunProfile, PersistentStateComponent<Element> {
  public BndRunConfigurationBase(Project project, @NotNull ConfigurationFactory factory, String name) {
    super(project, factory, name);
  }

  @Override
  protected BndRunConfigurationOptions getOptions() {
    return (BndRunConfigurationOptions)super.getOptions();
  }

  @Override
  public Element getState() {
    Element element = new Element("state");
    super.writeExternal(element);
    return element;
  }

  @NotNull
  @Override
  public SettingsEditor<? extends BndRunConfigurationBase> getConfigurationEditor() {
    return new BndRunConfigurationEditor(getProject());
  }

  @Nullable
  @Override
  public abstract RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException;

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

    @Nullable
    @Override
    public BndLaunchState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
      return new BndLaunchState(environment, this);
    }
  }

  public static class Test extends BndRunConfigurationBase {
    public Test(Project project, @NotNull ConfigurationFactory factory, String name) {
      super(project, factory, name);
    }

    @Nullable
    @Override
    public BndTestState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
      return new BndTestState(environment, this);
    }
  }
}

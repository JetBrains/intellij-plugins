// Copyright 2000-2017 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializer;
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
  protected Class<BndRunConfigurationOptions> getOptionsClass() {
    return BndRunConfigurationOptions.class;
  }

  @Override
  protected BndRunConfigurationOptions getOptions() {
    return (BndRunConfigurationOptions)super.getOptions();
  }

  @Override
  public Element getState() {
    Element element = new Element("state");
    super.writeState(element);
    return element;
  }

  @Override
  public void loadState(Element element) {
    super.readExternal(element);
    XmlSerializer.deserializeInto(this, element);
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

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartWebdevConfigurationEditorForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartWebdevConfiguration extends LocatableConfigurationBase {
  @NotNull DartWebdevParameters myParameters = new DartWebdevParameters();

  protected DartWebdevConfiguration(@NotNull final Project project,
                                    @NotNull final DartWebdevConfigurationType configType,
                                    @NotNull final String name) {
    super(project, configType.getConfigurationFactories()[0], name);
  }

  @NotNull
  public DartWebdevParameters getParameters() {
    return myParameters;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationError {
    getParameters().check(getProject());
  }

  @Override
  public DartWebdevConfiguration clone() {
    final DartWebdevConfiguration clone = (DartWebdevConfiguration)super.clone();
    clone.myParameters = myParameters.clone();
    return clone;
  }

  @NotNull
  @Override
  public SettingsEditor<DartWebdevConfiguration> getConfigurationEditor() {
    return new DartWebdevConfigurationEditorForm(getProject());
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull final Executor executor,
                                  @NotNull final ExecutionEnvironment env) throws ExecutionException {
    return new DartWebdevRunningState(env);
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myParameters, element, new SkipDefaultValuesSerializationFilters());
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    XmlSerializer.deserializeInto(myParameters, element);
  }
}


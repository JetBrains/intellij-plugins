// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.runner;

import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EmptyRunProfileState;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class CfmlRunConfiguration extends RunConfigurationBase<Element> implements LocatableConfiguration{
  private CfmlRunnerParameters myRunnerParameters = new CfmlRunnerParameters();
  private boolean fromDefaultHost = false;

  protected CfmlRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new CfmlRunConfigurationEditor();
  }

  protected CfmlRunnerParameters createRunnerParametersInstance() {
    return new CfmlRunnerParameters();
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = createRunnerParametersInstance();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    if (myRunnerParameters != null) {
      XmlSerializer.serializeInto(myRunnerParameters, element);
    }
  }

  @Override
  public RunConfiguration clone() {
    CfmlRunConfiguration clone = (CfmlRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
    return EmptyRunProfileState.INSTANCE;
  }

  public static void checkURL(String url) throws RuntimeConfigurationException {
    // check URL for correctness
    try {
      if (url == null) {
        throw new MalformedURLException("No start file specified or this file is invalid");
      }
      //noinspection ResultOfObjectAllocationIgnored
      new URL(url);
    }
    catch (MalformedURLException ignored) {
      throw new RuntimeConfigurationError("Incorrect URL"); //NON-NLS
    }
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    checkURL(myRunnerParameters.getUrl());
  }

  public CfmlRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }


  @Override
  public boolean isGeneratedName() {
    return Objects.equals(getName(), suggestedName());
  }

  @Nullable
  @Override
  public String suggestedName() {
    final String path = getRunnerParameters().getUrl();
    return StringUtil.isNotEmpty(path) ? PathUtil.getFileName(path) : null;
  }

  public void setFromDefaultHost(boolean generatedFromDefaultHost){
    fromDefaultHost = generatedFromDefaultHost;
  }

  public boolean isFromDefaultHost() {
    return fromDefaultHost;
  }

}

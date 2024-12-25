// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartWebdevConfigurationEditorForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartWebdevConfiguration extends LocatableConfigurationBase<DartWebdevConfiguration> {
  @NotNull DartWebdevParameters myParameters = new DartWebdevParameters();

  protected DartWebdevConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
    super(project, factory, name);
  }

  public @NotNull DartWebdevParameters getParameters() {
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

  @Override
  public @NotNull SettingsEditor<DartWebdevConfiguration> getConfigurationEditor() {
    return new DartWebdevConfigurationEditorForm(getProject());
  }

  @Override
  public @Nullable RunProfileState getState(final @NotNull Executor executor,
                                            final @NotNull ExecutionEnvironment env) throws ExecutionException {
    return new DartWebdevRunningState(env);
  }

  @Override
  public @Nullable String suggestedName() {
    // Attempt to compute the relative path to the html file, i.e. some "web/index.html"
    // If not successful, return at least the file name, i.e. some "index.html"
    final String htmlFilePath = myParameters.getHtmlFilePath();
    String htmlFilePathRelativeFromWorkingDir = null;
    try {
      VirtualFile workingDir = myParameters.getWorkingDirectory(getProject());
      htmlFilePathRelativeFromWorkingDir =
        htmlFilePath.startsWith(workingDir.getPath() + "/") ? htmlFilePath.substring(workingDir.getPath().length() + 1) : null;
    }
    catch (RuntimeConfigurationError ignore) {
    }

    if (StringUtil.isNotEmpty(htmlFilePathRelativeFromWorkingDir)) {
      return htmlFilePathRelativeFromWorkingDir;
    }

    return StringUtil.isEmpty(htmlFilePath) ? DartBundle.message("runner.web.app.configuration.name") : PathUtil.getFileName(htmlFilePath);
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

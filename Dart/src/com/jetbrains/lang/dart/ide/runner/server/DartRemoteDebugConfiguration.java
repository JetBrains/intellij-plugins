// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EmptyRunProfileState;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartRemoteDebugConfigurationEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartRemoteDebugConfiguration extends RunConfigurationBase<Element> implements RunConfigurationWithSuppressedDefaultRunAction {
  @NotNull DartRemoteDebugParameters myParameters = new DartRemoteDebugParameters();

  protected DartRemoteDebugConfiguration(final @NotNull Project project,
                                         final @NotNull DartRemoteDebugConfigurationType configType,
                                         final @NotNull String name) {
    super(project, configType.getConfigurationFactories()[0], name);
  }

  public @NotNull DartRemoteDebugParameters getParameters() {
    return myParameters;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationError {
    final String path = myParameters.getDartProjectPath();
    if (path.isEmpty()) {
      throw new RuntimeConfigurationError(DartBundle.message("dialog.message.dart.project.path.not.specified"));
    }

    final VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(path);
    if (dir == null || !dir.isDirectory()) {
      throw new RuntimeConfigurationError(DartBundle.message("dialog.message.folder.not.found", FileUtil.toSystemDependentName(path)));
    }

    if (!ProjectRootManager.getInstance(getProject()).getFileIndex().isInContent(dir)) {
      throw new RuntimeConfigurationError(
        DartBundle.message("dialog.message.folder.not.in.project.content", FileUtil.toSystemDependentName(path)));
    }
  }

  @Override
  public DartRemoteDebugConfiguration clone() {
    final DartRemoteDebugConfiguration clone = (DartRemoteDebugConfiguration)super.clone();
    clone.myParameters = myParameters.clone();
    return clone;
  }

  @Override
  public @NotNull SettingsEditor<DartRemoteDebugConfiguration> getConfigurationEditor() {
    return new DartRemoteDebugConfigurationEditor(getProject());
  }

  @Override
  public @Nullable RunProfileState getState(final @NotNull Executor executor,
                                            final @NotNull ExecutionEnvironment environment) {
    return EmptyRunProfileState.INSTANCE;
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


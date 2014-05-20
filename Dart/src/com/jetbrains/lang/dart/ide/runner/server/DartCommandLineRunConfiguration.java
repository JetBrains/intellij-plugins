package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCommandLineRunConfiguration extends DartRunConfigurationBase {
  private @NotNull DartCommandLineRunnerParameters myRunnerParameters = new DartCommandLineRunnerParameters();

  public DartCommandLineRunConfiguration(String name, Project project, DartCommandLineRunConfigurationType configurationType) {
    super(project, configurationType.getConfigurationFactories()[0], name);
  }

  @NotNull
  public DartCommandLineRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DartCommandLineConfigurationEditorForm(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();
    final Module module = findModule();
    if (module == null) {
      throw new RuntimeConfigurationException(DartBundle.message("dart.run.no.module", getName()));
    }
  }

  @Nullable
  public Module getModule() {
    try {
      return findModule();
    }
    catch (RuntimeConfigurationException e) {
      return null;
    }
  }

  @Nullable
  private Module findModule() throws RuntimeConfigurationException {
    final String filePath = myRunnerParameters.getFilePath();
    if (filePath == null) {
      return null;
    }
    String fileUrl = VfsUtilCore.pathToUrl(FileUtil.toSystemIndependentName(filePath));
    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
    if (file == null) {
      throw new RuntimeConfigurationException("Can't find module for " + filePath);
    }
    return ModuleUtilCore.findModuleForFile(file, getProject());
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final String filePath = myRunnerParameters.getFilePath();
    if (StringUtil.isEmpty(filePath)) {
      throw new ExecutionException("Empty file path");
    }
    return new DartCommandLineRunningState(env, myRunnerParameters, -1);
  }

  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element, new SkipDefaultValuesSerializationFilters());
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Nullable
  public String suggestedName() {
    final String filePath = myRunnerParameters.getFilePath();
    return filePath == null ? null : PathUtil.getFileName(filePath);
  }

  public DartCommandLineRunConfiguration clone() {
    final DartCommandLineRunConfiguration clone = (DartCommandLineRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }
}

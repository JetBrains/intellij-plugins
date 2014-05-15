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
import com.intellij.util.containers.hash.LinkedHashMap;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DartCommandLineRunConfiguration extends DartRunConfigurationBase {
  private @Nullable String myFilePath = null;
  private @Nullable String myVMOptions = null;
  private @Nullable String myArguments = null;
  private @Nullable String myWorkingDirectory = null;
  private @NotNull Map<String, String> myEnvs = new LinkedHashMap<String, String>();
  private boolean myIncludeParentEnvs = true;

  public DartCommandLineRunConfiguration(String name, Project project, DartCommandLineRunConfigurationType configurationType) {
    super(project, configurationType.getConfigurationFactories()[0], name);
  }

  @Nullable
  @Override
  public String getFilePath() {
    return myFilePath;
  }

  @Override
  public void setFilePath(final @Nullable String filePath) {
    myFilePath = filePath;
  }

  @Nullable
  public String getVMOptions() {
    return myVMOptions;
  }

  public void setVMOptions(final @Nullable String vmOptions) {
    myVMOptions = vmOptions;
  }

  @Nullable
  public String getArguments() {
    return myArguments;
  }

  public void setArguments(final @Nullable String arguments) {
    myArguments = arguments;
  }

  @Nullable
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  public void setWorkingDirectory(final @Nullable String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }

  @NotNull
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
  public Map<String, String> getEnvs() {
    return myEnvs;
  }

  public void setEnvs(@SuppressWarnings("NullableProblems") final Map<String, String> envs) {
    if (envs != null) { // null comes from old projects or if storage corrupted
      myEnvs = envs;
    }
  }

  public boolean isIncludeParentEnvs() {
    return myIncludeParentEnvs;
  }

  public void setIncludeParentEnvs(final boolean includeParentEnvs) {
    myIncludeParentEnvs = includeParentEnvs;
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
    if (myFilePath == null) {
      return null;
    }
    String fileUrl = VfsUtilCore.pathToUrl(FileUtil.toSystemIndependentName(myFilePath));
    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
    if (file == null) {
      throw new RuntimeConfigurationException("Can't find module for " + myFilePath);
    }
    return ModuleUtilCore.findModuleForFile(file, getProject());
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final String filePath = getFilePath();
    if (StringUtil.isEmpty(filePath)) {
      throw new ExecutionException("Empty file path");
    }
    final String workingDirectory = myWorkingDirectory != null ? myWorkingDirectory : PathUtil.getParentPath(filePath);
    return new DartCommandLineRunningState(env,
                                           filePath,
                                           StringUtil.notNullize(getVMOptions()),
                                           StringUtil.notNullize(getArguments()),
                                           workingDirectory,
                                           getEnvs(),
                                           isIncludeParentEnvs());
  }

  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(this, element);
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    XmlSerializer.deserializeInto(this, element);
  }

  @Nullable
  public String suggestedName() {
    return myFilePath == null ? null : PathUtil.getFileName(myFilePath);
  }

  public DartCommandLineRunConfiguration clone() {
    final DartCommandLineRunConfiguration clone = (DartCommandLineRunConfiguration)super.clone();
    clone.myEnvs = new LinkedHashMap<String, String>();
    clone.myEnvs.putAll(myEnvs);
    return clone;
  }
}

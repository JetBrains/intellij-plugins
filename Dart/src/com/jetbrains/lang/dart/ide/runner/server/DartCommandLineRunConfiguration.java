package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.LocatableConfigurationBase;
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
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartCommandLineRunConfiguration extends LocatableConfigurationBase {
  @Nullable
  private String myFilePath = null;
  @Nullable
  private String myVMOptions = null;
  @Nullable
  private String myArguments = null;

  public DartCommandLineRunConfiguration(String name, Project project, DartCommandLineRunConfigurationType configurationType) {
    super(project, configurationType.getConfigurationFactories()[0], name);
  }

  @Nullable
  public String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(@Nullable String fileUrl) {
    myFilePath = fileUrl;
  }

  @Nullable
  public String getVMOptions() {
    return myVMOptions;
  }

  public void setVMOptions(@Nullable String vmOptions) {
    myVMOptions = vmOptions;
  }

  @Nullable
  public String getArguments() {
    return myArguments;
  }

  public void setArguments(@Nullable String arguments) {
    myArguments = arguments;
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
    return new DartCommandLineRunningState(
      env,
      getModule(),
      filePath,
      StringUtil.notNullize(getVMOptions()),
      StringUtil.notNullize(getArguments())
    );
  }


  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(this, element);
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    XmlSerializer.deserializeInto(this, element);
  }
}

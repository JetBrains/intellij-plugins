package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.unittest.ui.DartUnitConfigurationEditorForm;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartUnitRunConfiguration extends RunConfigurationBase implements LocatableConfiguration {
  private final DartUnitRunnerParameters myRunnerParameters = new DartUnitRunnerParameters();

  protected DartUnitRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  public DartUnitRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DartUnitConfigurationEditorForm(getProject());
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final Project project = env.getProject();
    if (project == null) {
      throw new ExecutionException("Can't find project");
    }
    final String path = myRunnerParameters.getFilePath();
    if (path == null || VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path)) == null) {
      throw new ExecutionException("Can't find file: " + path);
    }
    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path));
    assert virtualFile != null;
    final Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
    if (module == null) {
      throw new ExecutionException("Can't find module for file");
    }
    final DartSettings settingsForModule = DartSettings.getSettingsForModule(module);
    return new DartUnitRunningState(env, myRunnerParameters, settingsForModule);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    final String path = myRunnerParameters.getFilePath();
    if (path == null || VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path)) == null) {
      throw new RuntimeConfigurationException("Can't find file: " + path);
    }
    if (!FileUtilRt.extensionEquals(path, DartFileType.DEFAULT_EXTENSION)) {
      throw new RuntimeConfigurationException("Not a Dart file");
    }
  }

  @Override
  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element);
  }

  @Override
  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Override
  public boolean isGeneratedName() {
    return "Unnamed".equals(getName());
  }

  @Override
  public String suggestedName() {
    return "Unnamed";
  }
}

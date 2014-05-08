package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
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
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.unittest.ui.DartUnitConfigurationEditorForm;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartUnitRunConfiguration extends DartRunConfigurationBase {

  private DartUnitRunnerParameters myRunnerParameters = new DartUnitRunnerParameters();

  protected DartUnitRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  @Override
  protected void filePathChanged(final @NotNull VirtualFile file) { myRunnerParameters.setFilePath(file.getPath()); }

  public DartUnitRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DartUnitConfigurationEditorForm(getProject());
  }

  @Nullable
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final Project project = env.getProject();
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
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      throw new ExecutionException("Dart SDK is not configured");
    }
    return new DartUnitRunningState(env, myRunnerParameters, sdk);
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

  public String suggestedName() {
    final String path = myRunnerParameters.getFilePath();
    if (path != null) {
      final String fileName = PathUtil.getFileName(path);
      switch (myRunnerParameters.getScope()) {
        case METHOD:
          return DartBundle.message("test.0.in.1", myRunnerParameters.getTestName(), fileName);
        case GROUP:
          return DartBundle.message("test.group.0.in.1", myRunnerParameters.getTestName(), fileName);
        case ALL:
          return DartBundle.message("all.tests.in.0", fileName);
      }
    }
    return null;
  }

  public RunConfiguration clone() {
    final DartUnitRunConfiguration clone = (DartUnitRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

}

package com.jetbrains.plugins.meteor.runner;

import com.intellij.configurationStore.XmlSerializer;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.browsers.StartBrowserSettings;
import com.intellij.javascript.debugger.DebuggableFileFinder;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfiguration;
import com.intellij.javascript.debugger.execution.DebuggableProcessRunConfigurationBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.nodeJs.NodeDebugProgramRunnerKt;
import com.jetbrains.nodeJs.NodeJSDebuggableConfiguration;
import com.jetbrains.nodeJs.NodeJsDebugProcess;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity;
import com.jetbrains.plugins.meteor.ide.action.MeteorPackagesUtil;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeteorRunConfiguration extends DebuggableProcessRunConfigurationBase implements NodeJSDebuggableConfiguration,
                                                                                             DebuggableProcessRunConfiguration,
                                                                                             RunProfileWithCompileBeforeLaunchOption {

  private static final String STATE_TAG_NAME = "meteor-runner-state";

  @Tag(STATE_TAG_NAME)
  private static class MeteorState {
    @Tag
    @Nullable
    public StartBrowserSettings startBrowserSettings;
  }

  @NotNull
  private MeteorState myState = new MeteorState();

  @NotNull
  @Override
  public DebuggableFileFinder createFileFinder(@NotNull Project project) {
    return new MeteorFileFinder(FileUtil.toSystemIndependentName(getEffectiveWorkingDirectory()));
  }

  @Nullable
  @Override
  protected String computeDefaultExePath() {
    return MeteorSettings.getInstance().getExecutablePath();
  }

  @NotNull
  @Override
  protected String getInputFileTitle() {
    return "";
  }

  @Override
  public String getExePath() {
    String path = super.getExePath();
    return path == null ? computeDefaultExePath() : path;
  }

  @NotNull
  @Override
  public XDebugProcess createDebugProcess(@NotNull InetSocketAddress socketAddress,
                                          @NotNull XDebugSession session,
                                          @Nullable ExecutionResult executionResult,
                                          @NotNull ExecutionEnvironment environment) {
    var connection = NodeDebugProgramRunnerKt.createRemoteConnection();
    DebuggableFileFinder fileFinder = createFileFinder(session.getProject());
    var process = new NodeJsDebugProcess(session, connection, fileFinder, true, executionResult);
    connection.open(socketAddress);
    return process;
  }

  @NotNull
  public StartBrowserSettings getStartBrowserSettings() {
    if (myState.startBrowserSettings == null) {
      myState.startBrowserSettings = new StartBrowserSettings();
      myState.startBrowserSettings.setUrl("http://localhost:3000");
    }

    return myState.startBrowserSettings;
  }

  public void setStartBrowserSettings(StartBrowserSettings settings) {
    myState.startBrowserSettings = settings;
  }

  protected MeteorRunConfiguration(Project project,
                                   ConfigurationFactory factory, String name) {
    super(project, factory, name);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new MeteorRunConfigurationEditor(getProject());
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
    return new MeteorRunProfileState(this, environment);
  }

  @Override
  public void setWorkingDirectory(@Nullable String value) {
    String resultDir =
      StringUtil.equals(PathUtil.toSystemDependentName(getProject().getBasePath()), PathUtil.toSystemDependentName(value)) ? null : value;
    super.setWorkingDirectory(resultDir);
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    myState = new MeteorState();
    final Element stateElement = element.getChild(STATE_TAG_NAME);
    if (stateElement != null) {
      XmlSerializer.deserializeInto(stateElement, myState);
    }
  }

  @Override
  public void writeExternal(@NotNull Element element) {
    super.writeExternal(element);

    Element child = XmlSerializer.serialize(myState);
    if (child != null) {
      element.addContent(child);
    }
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (!MeteorFacade.getInstance().isMeteorFolder(getEffectiveWorkingDirectory())) {
      throw new RuntimeConfigurationError(MeteorBundle.message("dialog.message.please.specify.path.to.meteor.working.directory.correctly"));
    }
  }

  @Override
  public boolean isBuildBeforeLaunchAddedByDefault() {
    return false;
  }

  private static final boolean IS_NODE8_DEFAULT = true;
  private static final String RELEASE_FILE_NAME = "release";
  private static final Pattern METEOR_VERSION_PATTERN = Pattern.compile("^METEOR@(\\d+)\\.(\\d+)");

  public boolean isNode8() {
    VirtualFile dotMeteorVirtualFile = findAssociatedMeteorDir();
    if (dotMeteorVirtualFile == null) {
      Logger.getInstance(MeteorRunConfiguration.class).debug("Cannot find .meteor folder");
      return IS_NODE8_DEFAULT;
    }

    final VirtualFile versionFile = MeteorPackagesUtil.getMeteorDirectoryFileByName(getProject(), dotMeteorVirtualFile, RELEASE_FILE_NAME);
    if (versionFile == null || !versionFile.isValid()) return IS_NODE8_DEFAULT;

    try {
      String versionText = VfsUtilCore.loadText(versionFile, 20);
      // not a semantic version, can be 1.5.2.2
      // normally, METEOR@1.6-rc.2
      Matcher matcher = METEOR_VERSION_PATTERN.matcher(versionText);
      if (matcher.find() && matcher.groupCount() >= 2) {
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        return major > 1 || major == 1 && minor >= 6;
      }
    }
    catch (IOException | NumberFormatException ignored) {}
    return IS_NODE8_DEFAULT;
  }

  @Nullable
  private VirtualFile findAssociatedMeteorDir() {
    VirtualFile dotMeteorVirtualFile = null;
    String workingDirectory = getEffectiveWorkingDirectory();
    if (workingDirectory != null) {
      VirtualFile workingDir = LocalFileSystem.getInstance().findFileByPath(workingDirectory);
      if (workingDir != null) {
        dotMeteorVirtualFile = workingDir.findChild(MeteorProjectStartupActivity.METEOR_FOLDER);
      }
    }
    if (dotMeteorVirtualFile == null) {
      dotMeteorVirtualFile = MeteorPackagesUtil.getDotMeteorVirtualFile(getProject(), null);
    }
    return dotMeteorVirtualFile;
  }
}

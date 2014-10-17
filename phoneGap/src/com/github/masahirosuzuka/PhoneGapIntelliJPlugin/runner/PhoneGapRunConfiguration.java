package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapTargets;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor;
import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsole;
import com.intellij.diagnostic.logging.LogFilterModel;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_ANDROID;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_IOS;

/**
 * PhoneGapRunConfiguration.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/05.
 */
public class PhoneGapRunConfiguration extends LocatableConfigurationBase {

  private static final String CORDOVA_IOS_LOG_PATH = "/platforms/ios/cordova/console.log";
  //public for serializer
  @Nullable
  public String myExecutable;

  @Nullable
  public String myWorkDir;

  @Nullable
  public String myCommand;

  @Nullable
  public String getCommand() {
    return myCommand;
  }

  @Nullable
  public String myPlatform;


  public String getExtraArgs() {
    return myExtraArgs;
  }

  public void setExtraArgs(String extraArgs) {
    myExtraArgs = extraArgs;
  }

  public String myExtraArgs;

  public boolean hasTarget() {
    return hasTarget;
  }

  public void setHasTarget(boolean hasTarget) {
    this.hasTarget = hasTarget;
  }

  @Nullable
  public String getTarget() {
    return target;
  }

  public void setTarget(@Nullable String target) {
    this.target = target;
  }

  public boolean hasTarget;

  @Nullable
  public String target;

  private volatile PhoneGapCommandLine myCommandLine;

  @Nullable
  public String getWorkDir() {
    return myWorkDir;
  }

  public void setWorkDir(@Nullable String workDir) {
    this.myWorkDir = workDir;
  }

  @Nullable
  public String getExecutable() {
    return myExecutable;
  }

  public void setExecutable(@Nullable String executable) {
    myExecutable = executable;
  }

  public void setCommand(@Nullable String myCommand) {
    this.myCommand = myCommand;
  }

  @Nullable
  public String getPlatform() {
    return myPlatform;
  }

  public void setPlatform(@Nullable String myPlatform) {
    this.myPlatform = myPlatform;
  }


  public PhoneGapRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);

    //defaults
  }

  @Override
  public String suggestedName() {
    return "Run PhoneGap";
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);

    //noinspection deprecation
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);

    //noinspection deprecation
    DefaultJDOMExternalizer.writeExternal(this, element);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new PhoneGapRunConfigurationEditor(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (StringUtil.isEmpty(myCommand)) {
      throw new RuntimeConfigurationError("Command is missing");
    }

    if (StringUtil.isEmpty(myPlatform)) {
      throw new RuntimeConfigurationError("Platform is missing");
    }

    if (StringUtil.isEmpty(myExecutable)) {
      throw new RuntimeConfigurationError("Executable is missing");
    }

    if (StringUtil.isEmpty(myWorkDir)) {
      throw new RuntimeConfigurationError("Working directory is missing");
    }

    if (myPlatform.equals(PLATFORM_ANDROID)) {
      checkExistsSdkWithWarning(PhoneGapTargets.getAndroidName(), "Cannot detect android SDK in path");
    }
    if (myPlatform.equals(PLATFORM_IOS)) {
      checkExistsSdkWithWarning(PhoneGapTargets.getIosSimName(), "Cannot detect ios-sim in path");
    }
  }

  public PhoneGapCommandLine getCommandLine() {
    PhoneGapCommandLine current = myCommandLine;
    String executable = getExecutable();
    String workDir = getWorkDir();
    if (current != null && StringUtil.equals(current.getPath(), executable) && StringUtil.equals(current.getWorkDir(), workDir)) {
      return current;
    }
    assert executable != null;
    assert workDir != null;

    current = new PhoneGapCommandLine(executable, workDir);
    myCommandLine = current;

    return current;
  }

  @SuppressWarnings("CloneDoesntCallSuperClone")
  @Override
  public PhoneGapRunConfiguration clone() {
    final Element element = new Element("toClone");
    try {
      writeExternal(element);
      PhoneGapRunConfiguration configuration =
        (PhoneGapRunConfiguration)getFactory().createTemplateConfiguration(getProject());
      configuration.setName(getName());
      configuration.readExternal(element);
      return configuration;
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor,
                                  @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {

    return new PhoneGapRunProfileState(getProject(), executionEnvironment, this);
  }

  private static void checkExistsSdkWithWarning(@Nullable String path, @NotNull String error) throws RuntimeConfigurationWarning {
    if (path == null) return;

    File file = PathEnvironmentVariableUtil.findInPath(path);
    if (file != null && file.exists()) {
      return;
    }

    throw new RuntimeConfigurationWarning(error);
  }

  @Override
  public final void customizeLogConsole(final LogConsole console) {
    LogFilterModel model = console.getFilterModel();
    if( model instanceof DefaultLogFilterModel) {
      ((DefaultLogFilterModel)model).setCheckStandartFilters(false);
    }
  }

  @NotNull
  @Override
  public ArrayList<LogFileOptions> getAllLogFiles() {
    if (!PLATFORM_IOS.equals(myPlatform) || StringUtil.isEmpty(myWorkDir)) super.getAllLogFiles();
    return ContainerUtil.newArrayList(new LogFileOptions("console.log", getPathToLog(), true, false, true));
  }

  private String getPathToLog() {
    assert myWorkDir != null;
    return FileUtil
      .toCanonicalPath(FileUtil.toSystemIndependentName(StringUtil.trimEnd(myWorkDir, File.separator)) + CORDOVA_IOS_LOG_PATH);
  }
}

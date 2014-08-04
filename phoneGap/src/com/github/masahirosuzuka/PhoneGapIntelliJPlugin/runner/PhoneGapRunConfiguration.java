package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.PhoneGapRunConfigurationEditor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PhoneGapRunConfiguration.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/05.
 */
public class PhoneGapRunConfiguration extends LocatableConfigurationBase {

  //public for serializer
  @Nullable
  public String myExecutable;

  @Nullable
  public String myCommand;

  @Nullable
  public String getCommand() {
    return myCommand;
  }

  @Nullable
  public String myPlatform;

  private volatile PhoneGapCommandLine myCommandLine;

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

    DefaultJDOMExternalizer.readExternal(this, element);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);

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
      throw new RuntimeConfigurationException("Command is missing");
    }

    if (StringUtil.isEmpty(myPlatform)) {
      throw new RuntimeConfigurationException("Platform is missing");
    }

    if (StringUtil.isEmpty(myExecutable)) {
      throw new RuntimeConfigurationException("Executable is missing");
    }
  }

  public PhoneGapCommandLine getCommandLine() {
    PhoneGapCommandLine current = myCommandLine;
    String executable = getExecutable();
    if (current != null && StringUtil.equals(current.getPath(), executable)) {
      return current;
    }
    assert executable != null;
    current = new PhoneGapCommandLine(executable, getProject().getBasePath());
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
}

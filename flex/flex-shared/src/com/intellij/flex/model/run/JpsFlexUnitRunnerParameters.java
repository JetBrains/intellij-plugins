// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.run;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexDependencyEntry;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.util.JpsPathUtil;

import java.util.Collections;

public class JpsFlexUnitRunnerParameters extends JpsBCBasedRunnerParameters<JpsFlexUnitRunnerParameters> {

  public enum Scope {
    Method, Class, Package
  }

  public enum OutputLogLevel {
    Fatal("1000"), Error("8"), Warn("6"), Info("4"), Debug("2"), All("0");

    private final String myFlexConstant;

    OutputLogLevel(String flexConstant) {
      myFlexConstant = flexConstant;
    }

    public String getFlexConstant() {
      return myFlexConstant;
    }
  }

  private @NotNull Scope myScope = Scope.Class;

  private @NotNull String myPackageName = "";
  private @NotNull String myClassName = "";
  private @NotNull String myMethodName = "";

  private @Nullable OutputLogLevel myOutputLogLevel = null;
  //private @NotNull LauncherParameters myLauncherParameters = new LauncherParameters();
  //private boolean myTrusted = true;

  //private String myEmulatorAdlOptions = "";
  //private @NotNull AppDescriptorForEmulator myAppDescriptorForEmulator = AppDescriptorForEmulator.Android;

  private int myPort;
  private int mySocketPolicyPort;

  public JpsFlexUnitRunnerParameters() {
  }

  private JpsFlexUnitRunnerParameters(final JpsFlexUnitRunnerParameters original) {
    super(original);

    myScope = original.myScope;
    myPackageName = original.myPackageName;
    myClassName = original.myClassName;
    myMethodName = original.myMethodName;
    myOutputLogLevel = original.myOutputLogLevel;
  }

  @Override
  public @NotNull JpsFlexUnitRunnerParameters createCopy() {
    return new JpsFlexUnitRunnerParameters(this);
  }

  // ------------------

  @Attribute("scope")
  public @NotNull Scope getScope() {
    return myScope;
  }

  public void setScope(@NotNull Scope scope) {
    myScope = scope;
  }

  @Attribute("package_name")
  public @NotNull String getPackageName() {
    return myPackageName;
  }

  public void setPackageName(@NotNull String packageName) {
    myPackageName = packageName;
  }

  @Attribute("class_name")
  public @NotNull String getClassName() {
    return myClassName;
  }

  public void setClassName(@NotNull String className) {
    myClassName = className;
  }

  @Attribute("method_name")
  public @NotNull String getMethodName() {
    return myMethodName;
  }

  public void setMethodName(@NotNull String methodName) {
    myMethodName = methodName;
  }

  @Attribute("output_log_level")
  public @Nullable OutputLogLevel getOutputLogLevel() {
    return myOutputLogLevel;
  }

  public void setOutputLogLevel(@Nullable OutputLogLevel outputLogLevel) {
    myOutputLogLevel = outputLogLevel;
  }

  @Transient
  public int getPort() {
    return myPort;
  }

  public void setPort(int port) {
    myPort = port;
  }

  @Transient
  public int getSocketPolicyPort() {
    return mySocketPolicyPort;
  }

  public void setSocketPolicyPort(int port) {
    mySocketPolicyPort = port;
  }

  @Override
  public @Nullable JpsFlexBuildConfiguration getBC(final JpsProject project) {
    final JpsFlexBuildConfiguration bc = super.getBC(project);
    if (bc == null) return null;

    final JpsFlexBuildConfiguration overriddenBC = bc.getModule().getProperties().createTemporaryCopyForCompilation(bc);
    overriddenBC.setOutputType(OutputType.Application);

    overriddenBC.setMainClass(FlexCommonUtils.FLEX_UNIT_LAUNCHER);
    overriddenBC.setOutputFileName("_flexunit.swf");
    final String testOutputUrl = StringUtil.notNullize(JpsJavaExtensionService.getInstance().getOutputUrl(bc.getModule(), true));
    overriddenBC.setOutputFolder(JpsPathUtil.urlToPath(testOutputUrl));

    overriddenBC.setUseHtmlWrapper(false);
    overriddenBC.setRLMs(Collections.emptyList());
    overriddenBC.setCssFilesToCompile(Collections.emptyList());
    overriddenBC.setSkipCompile(false);

    overriddenBC.getDependencies().setFrameworkLinkage(LinkageType.Merged);

    for (JpsFlexDependencyEntry entry : overriddenBC.getDependencies().getEntries()) {
      if (entry.getLinkageType() == LinkageType.External) {
        entry.setLinkageType(LinkageType.Merged);
      }
    }

    return overriddenBC;
  }
}

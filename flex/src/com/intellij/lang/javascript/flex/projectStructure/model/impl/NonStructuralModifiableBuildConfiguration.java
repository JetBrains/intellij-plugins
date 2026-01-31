// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.AirDesktopPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AndroidPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.Dependencies;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.IosPackagingOptions;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.Collection;

public class NonStructuralModifiableBuildConfiguration implements FlexBuildConfiguration {

  private final FlexBuildConfigurationImpl myOriginal;

  NonStructuralModifiableBuildConfiguration(final FlexBuildConfigurationImpl original) {
    myOriginal = original;
  }

  @Override
  public @NotNull Dependencies getDependencies() {
    return myOriginal.getDependencies();
  }

  @Override
  public @NotNull NonStructuralModifiableCompilerOptions getCompilerOptions() {
    return new NonStructuralModifiableCompilerOptions(myOriginal.getCompilerOptions());
  }

  @Override
  public @NotNull String getName() {
    return myOriginal.getName();
  }

  public void setCssFilesToCompile(final Collection<String> cssFilesToCompile) {
    myOriginal.setCssFilesToCompile(cssFilesToCompile);
  }

  public void setRLMs(final @NotNull Collection<RLMInfo> rlms) {
    myOriginal.setRLMs(rlms);
  }

  @Override
  public @NotNull TargetPlatform getTargetPlatform() {
    return myOriginal.getTargetPlatform();
  }

  @Override
  public @NotNull OutputType getOutputType() {
    return myOriginal.getOutputType();
  }

  @Override
  public boolean isPureAs() {
    return myOriginal.isPureAs();
  }

  @Override
  public @NotNull String getOptimizeFor() {
    return myOriginal.getOptimizeFor();
  }

  @Override
  public @NotNull String getMainClass() {
    return myOriginal.getMainClass();
  }

  public void setMainClass(final @NotNull String mainClass) {
    myOriginal.setMainClass(mainClass);
  }

  @Override
  public @NotNull String getOutputFileName() {
    return myOriginal.getOutputFileName();
  }

  @Override
  public @NotNull String getOutputFolder() {
    return myOriginal.getOutputFolder();
  }

  @Override
  public boolean isUseHtmlWrapper() {
    return myOriginal.isUseHtmlWrapper();
  }

  @Override
  public @NotNull String getWrapperTemplatePath() {
    return myOriginal.getWrapperTemplatePath();
  }

  @Override
  public @NotNull Collection<RLMInfo> getRLMs() {
    return myOriginal.getRLMs();
  }

  @Override
  public @NotNull Collection<String> getCssFilesToCompile() {
    return myOriginal.getCssFilesToCompile();
  }

  @Override
  public boolean isSkipCompile() {
    return myOriginal.isSkipCompile();
  }

  @Override
  public @NotNull Icon getIcon() {
    return myOriginal.getIcon();
  }

  @Override
  public String getActualOutputFilePath() {
    return myOriginal.getActualOutputFilePath();
  }

  @Override
  public BuildConfigurationNature getNature() {
    return myOriginal.getNature();
  }

  @Override
  public Sdk getSdk() {
    return myOriginal.getSdk();
  }

  @Override
  public boolean isTempBCForCompilation() {
    return myOriginal.isTempBCForCompilation();
  }

  @Override
  public boolean isEqual(final FlexBuildConfiguration other) {
    return myOriginal.isEqual(other);
  }

  @Override
  public @NotNull IosPackagingOptions getIosPackagingOptions() {
    return myOriginal.getIosPackagingOptions();
  }

  @Override
  public @NotNull AirDesktopPackagingOptions getAirDesktopPackagingOptions() {
    return myOriginal.getAirDesktopPackagingOptions();
  }

  @Override
  public @NotNull AndroidPackagingOptions getAndroidPackagingOptions() {
    return myOriginal.getAndroidPackagingOptions();
  }

  @Override
  public String getShortText() {
    return myOriginal.getShortText();
  }

  @Override
  public String getDescription() {
    return myOriginal.getDescription();
  }

  @Override
  public String getStatisticsEntry() {
    return myOriginal.getStatisticsEntry();
  }
}

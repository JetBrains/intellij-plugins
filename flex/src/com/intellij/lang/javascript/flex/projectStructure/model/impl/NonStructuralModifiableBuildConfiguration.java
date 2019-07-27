package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

public class NonStructuralModifiableBuildConfiguration implements FlexBuildConfiguration {

  private final FlexBuildConfigurationImpl myOriginal;

  NonStructuralModifiableBuildConfiguration(final FlexBuildConfigurationImpl original) {
    myOriginal = original;
  }

  @NotNull
  @Override
  public Dependencies getDependencies() {
    return myOriginal.getDependencies();
  }

  @NotNull
  @Override
  public NonStructuralModifiableCompilerOptions getCompilerOptions() {
    return new NonStructuralModifiableCompilerOptions(myOriginal.getCompilerOptions());
  }

  public void setMainClass(@NotNull final String mainClass) {
    myOriginal.setMainClass(mainClass);
  }

  public void setCssFilesToCompile(final Collection<String> cssFilesToCompile) {
    myOriginal.setCssFilesToCompile(cssFilesToCompile);
  }

  public void setRLMs(final @NotNull Collection<RLMInfo> rlms) {
    myOriginal.setRLMs(rlms);
  }

  @Override
  @NotNull
  public String getName() {
    return myOriginal.getName();
  }

  @NotNull
  @Override
  public TargetPlatform getTargetPlatform() {
    return myOriginal.getTargetPlatform();
  }

  @Override
  public boolean isPureAs() {
    return myOriginal.isPureAs();
  }

  @NotNull
  @Override
  public OutputType getOutputType() {
    return myOriginal.getOutputType();
  }

  @NotNull
  @Override
  public String getOptimizeFor() {
    return myOriginal.getOptimizeFor();
  }

  @NotNull
  @Override
  public String getMainClass() {
    return myOriginal.getMainClass();
  }

  @NotNull
  @Override
  public String getOutputFileName() {
    return myOriginal.getOutputFileName();
  }

  @NotNull
  @Override
  public String getOutputFolder() {
    return myOriginal.getOutputFolder();
  }

  @Override
  public boolean isUseHtmlWrapper() {
    return myOriginal.isUseHtmlWrapper();
  }

  @NotNull
  @Override
  public String getWrapperTemplatePath() {
    return myOriginal.getWrapperTemplatePath();
  }

  @NotNull
  @Override
  public Collection<RLMInfo> getRLMs() {
    return myOriginal.getRLMs();
  }

  @NotNull
  @Override
  public Collection<String> getCssFilesToCompile() {
    return myOriginal.getCssFilesToCompile();
  }

  @Override
  public boolean isSkipCompile() {
    return myOriginal.isSkipCompile();
  }

  @NotNull
  @Override
  public Icon getIcon() {
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

  @NotNull
  @Override
  public IosPackagingOptions getIosPackagingOptions() {
    return myOriginal.getIosPackagingOptions();
  }

  @NotNull
  @Override
  public AirDesktopPackagingOptions getAirDesktopPackagingOptions() {
    return myOriginal.getAirDesktopPackagingOptions();
  }

  @NotNull
  @Override
  public AndroidPackagingOptions getAndroidPackagingOptions() {
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

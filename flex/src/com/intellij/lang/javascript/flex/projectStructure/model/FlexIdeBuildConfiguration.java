package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ksafonov
 */
public interface FlexIdeBuildConfiguration {

  @NotNull
  String getName();

  @NotNull
  TargetPlatform getTargetPlatform();

  boolean isPureAs();

  @NotNull
  OutputType getOutputType();

  @NotNull
  String getOptimizeFor();

  @NotNull
  String getMainClass();

  @NotNull
  String getOutputFileName();

  @NotNull
  String getOutputFolder();

  boolean isUseHtmlWrapper();

  @NotNull
  String getWrapperTemplatePath();

  boolean isSkipCompile();

  @NotNull
  Dependencies getDependencies();

  @NotNull
  CompilerOptions getCompilerOptions();

  @NotNull
  AirDesktopPackagingOptions getAirDesktopPackagingOptions();

  @NotNull
  AndroidPackagingOptions getAndroidPackagingOptions();

  @NotNull
  IosPackagingOptions getIosPackagingOptions();

  Icon getIcon();

  String getOutputFilePath();

  BuildConfigurationNature getNature();
}

package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public interface ModifiableFlexIdeBuildConfiguration extends FlexIdeBuildConfiguration {

  @NotNull
  @Override
  ModifiableDependencies getDependencies();

  @NotNull
  @Override
  ModifiableCompilerOptions getCompilerOptions();

  @NotNull
  @Override
  ModifiableAirDesktopPackagingOptions getAirDesktopPackagingOptions();

  @NotNull
  @Override
  ModifiableAndroidPackagingOptions getAndroidPackagingOptions();

  @NotNull
  @Override
  ModifiableIosPackagingOptions getIosPackagingOptions();

  void setTargetPlatform(@NotNull TargetPlatform targetPlatform);

  void setNature(BuildConfigurationNature nature);

  void setPureAs(boolean pureAs);

  void setOutputType(@NotNull OutputType outputType);

  void setOptimizeFor(@NotNull String optimizeFor);

  void setMainClass(@NotNull String mainClass);

  void setOutputFileName(@NotNull String outputFileName);

  void setOutputFolder(@NotNull String outputFolder);

  void setUseHtmlWrapper(boolean useHtmlWrapper);

  void setWrapperTemplatePath(@NotNull String wrapperTemplatePath);

  void setCssFilesToCompile(@NotNull String cssFilesToCompile);

  void setSkipCompile(boolean skipCompile);

  void setName(@NotNull String name);
}

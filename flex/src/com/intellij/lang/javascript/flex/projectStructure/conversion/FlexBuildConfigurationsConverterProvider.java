package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.ConversionContext;
import com.intellij.conversion.ConverterProvider;
import com.intellij.conversion.ProjectConverter;
import com.intellij.lang.javascript.flex.FlexBundle;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public class FlexBuildConfigurationsConverterProvider extends ConverterProvider {
  protected FlexBuildConfigurationsConverterProvider() {
    super("flex-build-configurations");
  }

  @NotNull
  @Override
  public String getConversionDescription() {
    return FlexBundle.message("project.converter.description");
  }

  @NotNull
  @Override
  public ProjectConverter createConverter(@NotNull ConversionContext context) {
    return new FlexProjectConverter(context);
  }
}

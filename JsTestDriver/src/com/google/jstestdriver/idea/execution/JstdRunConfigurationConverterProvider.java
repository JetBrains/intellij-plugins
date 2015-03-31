package com.google.jstestdriver.idea.execution;

import com.intellij.conversion.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class JstdRunConfigurationConverterProvider extends ConverterProvider {

  private static final String OLD_JSTD_TYPE_VALUE = "JSTestDriver:ConfigurationType";
  private static final String ATTR_TYPE_NAME = "type";

  public JstdRunConfigurationConverterProvider() {
    super("jstestdriver-run-configurations-converter");
  }

  @NotNull
  @Override
  public String getConversionDescription() {
    return "JsTestDriver run configurations will be converted into a new format";
  }

  @NotNull
  @Override
  public ProjectConverter createConverter(@NotNull ConversionContext context) {
    return new ProjectConverter() {
      @Override
      public ConversionProcessor<RunManagerSettings> createRunConfigurationsConverter() {
        return new ConversionProcessor<RunManagerSettings>() {
          @Override
          public boolean isConversionNeeded(RunManagerSettings settings) {
            for (Element element : settings.getRunConfigurations()) {
              if (!"true".equals(element.getAttributeValue("default"))) {
                String type = element.getAttributeValue(ATTR_TYPE_NAME);
                if (OLD_JSTD_TYPE_VALUE.equals(type)) {
                  return true;
                }
              }
            }
            return false;
          }

          @Override
          public void process(RunManagerSettings settings) throws CannotConvertException {
            for (Element element : settings.getRunConfigurations()) {
              String type = element.getAttributeValue(ATTR_TYPE_NAME);
              if (OLD_JSTD_TYPE_VALUE.equals(type)) {
                element.setAttribute(ATTR_TYPE_NAME, JstdConfigurationType.ID);
              }
            }
          }
        };
      }
    };
  }
}

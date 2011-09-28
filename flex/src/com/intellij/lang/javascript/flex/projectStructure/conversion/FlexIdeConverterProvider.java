package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.ConversionContext;
import com.intellij.conversion.ConverterProvider;
import com.intellij.conversion.ProjectConverter;
import com.intellij.openapi.application.ApplicationNamesInfo;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public class FlexIdeConverterProvider extends ConverterProvider {


  protected FlexIdeConverterProvider() {
    super("flex-ide");
  }

  @NotNull
  @Override
  public String getConversionDescription() {
    return "Flex modules configuration will be transformed";
  }

  @Override
  public String getConversionDialogText(ConversionContext context) {
    return "Project '" +
           context.getProjectFile().getName() +
           "' needs to be converted to be opened in " +
           ApplicationNamesInfo.getInstance().getProductName() + ". Converted project will remain compatible with IntelliJ IDEA.";
  }

  @NotNull
  @Override
  public ProjectConverter createConverter(@NotNull ConversionContext context) {
    return new FlexIdeConverter(context);
  }
}

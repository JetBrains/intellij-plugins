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
    return "Flex modules configuration will be updated.<br/>Java modules with Flex facets will be converted to Flex modules.<br/>";
  }

  @Override
  public String getConversionDialogText(ConversionContext context) {
    return "Project '" +
           context.getProjectFile().getName() +
           "' needs to be converted to be opened in " +
           ApplicationNamesInfo.getInstance().getProductName() + ".<br/><b>Warning:</b>&nbsp;converted project will become incompatible with IntelliJ IDEA.";
  }

  @NotNull
  @Override
  public ProjectConverter createConverter(@NotNull ConversionContext context) {
    return new FlexIdeProjectConverter(context);
  }
}

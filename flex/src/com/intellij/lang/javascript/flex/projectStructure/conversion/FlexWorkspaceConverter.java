package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionProcessor;
import com.intellij.conversion.WorkspaceSettings;
import com.intellij.lang.javascript.flex.run.FlexIdeRunConfigurationType;
import com.intellij.openapi.util.Pair;
import org.jdom.Element;

import java.util.Collection;

public class FlexWorkspaceConverter extends ConversionProcessor<WorkspaceSettings> {
  private final ConversionParams myParams;

  public FlexWorkspaceConverter(final ConversionParams params) {
    myParams = params;
  }

  public boolean isConversionNeeded(final WorkspaceSettings workspaceSettings) {
    return true;
  }

  public void process(final WorkspaceSettings workspaceSettings) throws CannotConvertException {
  }

  public void postProcess(final WorkspaceSettings workspaceSettings) throws CannotConvertException {
    final Collection<Pair<String, String>> moduleAndBCNames = myParams.getAppModuleAndBCNames();
    if (moduleAndBCNames.isEmpty()) return;

    Element runManagerComponent = workspaceSettings.getComponentElement("RunManager");

    if (runManagerComponent == null) {
      runManagerComponent = new Element("component");
      runManagerComponent.setAttribute("name", "RunManager");
      workspaceSettings.getRootElement().addContent(runManagerComponent);
    }

    for (final Pair<String, String> moduleAndBCName : moduleAndBCNames) {
      createRunConfiguration(runManagerComponent, moduleAndBCName.first, moduleAndBCName.second);
    }
  }

  private static void createRunConfiguration(final Element runManagerComponent, final String moduleName, final String bcName) {
    final Element configurationElement = new Element("configuration");

    configurationElement.setAttribute("default", "false");
    configurationElement.setAttribute("name", bcName);
    configurationElement.setAttribute("type", FlexIdeRunConfigurationType.TYPE);
    configurationElement.setAttribute("factoryName", FlexIdeRunConfigurationType.DISPLAY_NAME);

    final Element moduleNameOption = new Element("option");
    moduleNameOption.setAttribute("name", "moduleName");
    moduleNameOption.setAttribute("value", moduleName);
    configurationElement.addContent(moduleNameOption);

    final Element bcNameOption = new Element("option");
    bcNameOption.setAttribute("name", "BCName");
    bcNameOption.setAttribute("value", bcName);
    configurationElement.addContent(bcNameOption);

    runManagerComponent.addContent(configurationElement);

    runManagerComponent.setAttribute("selected", FlexIdeRunConfigurationType.DISPLAY_NAME + "." + bcName);
  }
}

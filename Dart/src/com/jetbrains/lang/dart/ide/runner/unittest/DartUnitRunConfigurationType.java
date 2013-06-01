package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.DartBundle;

/**
 * @author: Fedor.Korotkov
 */
public class DartUnitRunConfigurationType extends ConfigurationTypeBase {
  protected DartUnitRunConfigurationType() {
    super("DartUnitRunConfigurationType",
          DartBundle.message("runner.unit.configuration.name"),
          DartBundle.message("runner.unit.configuration.description"),
          icons.DartIcons.Dart_16);
    addFactory(new DartUnitConfigurationFactory(this));
  }

  public static DartUnitRunConfigurationType getInstance() {
    return Extensions.findExtension(CONFIGURATION_TYPE_EP, DartUnitRunConfigurationType.class);
  }

  public static class DartUnitConfigurationFactory extends ConfigurationFactory {
    protected DartUnitConfigurationFactory(DartUnitRunConfigurationType type) {
      super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
      return new DartUnitRunConfiguration(project, this, "Dart");
    }
  }
}

package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class DartUnitRunConfigurationType extends ConfigurationTypeBase {
  protected DartUnitRunConfigurationType() {
    super("DartUnitRunConfigurationType",
          DartBundle.message("runner.unit.configuration.name"),
          DartBundle.message("runner.unit.configuration.description"),
          DartIcons.Dart_test);
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

    @Override
    public boolean isApplicable(@NotNull Project project) {
      return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    }
  }
}

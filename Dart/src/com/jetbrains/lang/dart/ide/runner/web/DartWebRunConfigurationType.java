package com.jetbrains.lang.dart.ide.runner.web;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class DartWebRunConfigurationType extends ConfigurationTypeBase {

  public static DartWebRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DartWebRunConfigurationType.class);
  }

  protected DartWebRunConfigurationType() {
    super("DartWebRunConfigurationType", DartBundle.message("runner.web.configuration.name"),
          DartBundle.message("runner.web.configuration.description"),
          DartIcons.Dart_web);
    addFactory(new ConfigurationFactory(this) {
      @Override
      public String getName() {
        return "Dart Web App"; // compatibility
      }

      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new DartWebRunConfiguration("Dart", project, DartWebRunConfigurationType.this);
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }
    });
  }
}

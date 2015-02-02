package com.jetbrains.lang.dart.ide.runner.server;

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

public class DartRemoteDebugConfigurationType extends ConfigurationTypeBase {
  public static DartRemoteDebugConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DartRemoteDebugConfigurationType.class);
  }

  public DartRemoteDebugConfigurationType() {
    super("DartRemoteDebugConfigurationType",
          DartBundle.message("remote.debug.configuration.name"),
          DartBundle.message("remote.debug.configuration.description"),
          DartIcons.Dart_16);
    addFactory(new ConfigurationFactory(this) {
      @Override
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new DartRemoteDebugConfiguration(project, DartRemoteDebugConfigurationType.this, "Dart Remote");
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }
    });
  }
}
